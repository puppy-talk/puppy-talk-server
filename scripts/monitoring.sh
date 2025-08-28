#!/bin/bash

# PuppyTalk Monitoring Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="puppytalk-server"
LOG_RETENTION_DAYS=7

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# System health check
system_health() {
    log_info "Checking system health..."
    echo
    
    # Check disk space
    log_info "Disk Usage:"
    df -h | grep -E "(Filesystem|/dev/)"
    echo
    
    # Check memory usage
    log_info "Memory Usage:"
    free -h
    echo
    
    # Check CPU load
    log_info "System Load:"
    uptime
    echo
    
    # Check Docker resources
    if command -v docker &> /dev/null; then
        log_info "Docker System Info:"
        docker system df
        echo
    fi
}

# Application metrics
application_metrics() {
    log_info "Checking application metrics..."
    echo
    
    # Check application health endpoint
    if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
        log_success "Application health endpoint is responding"
        
        # Get detailed health information
        log_info "Health Details:"
        curl -s http://localhost:8080/actuator/health | python3 -m json.tool 2>/dev/null || echo "Health endpoint active"
        echo
    else
        log_warning "Application health endpoint is not responding"
    fi
    
    # Check application info
    if curl -f -s http://localhost:8080/actuator/info > /dev/null; then
        log_info "Application Info:"
        curl -s http://localhost:8080/actuator/info | python3 -m json.tool 2>/dev/null || echo "Info endpoint active"
        echo
    fi
    
    # Check database connectivity
    if docker exec puppytalk-mysql-prod mysqladmin ping -h localhost -u root -p"${DB_ROOT_PASSWORD}" &> /dev/null; then
        log_success "Database connection is healthy"
    else
        log_warning "Database connection issues detected"
    fi
}

# Container status
container_status() {
    log_info "Checking container status..."
    echo
    
    if docker-compose -f docker-compose.prod.yml ps; then
        echo
        log_info "Container resource usage:"
        docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
    else
        log_error "Failed to get container status"
    fi
}

# Log analysis
log_analysis() {
    log_info "Analyzing recent logs..."
    echo
    
    # Application logs (last 50 lines)
    log_info "Recent Application Logs (last 50 lines):"
    docker-compose -f docker-compose.prod.yml logs --tail=50 app
    echo
    
    # Check for errors in logs
    error_count=$(docker-compose -f docker-compose.prod.yml logs --since="1h" app 2>/dev/null | grep -i "error\|exception\|failed" | wc -l)
    if [ "$error_count" -gt 0 ]; then
        log_warning "Found $error_count errors in the last hour"
        log_info "Recent errors:"
        docker-compose -f docker-compose.prod.yml logs --since="1h" app | grep -i "error\|exception\|failed" | tail -10
    else
        log_success "No errors found in recent logs"
    fi
}

# Database maintenance
database_maintenance() {
    log_info "Performing database maintenance..."
    
    # Create backup directory
    backup_dir="./backups/$(date +%Y%m%d)"
    mkdir -p "$backup_dir"
    
    # Database backup
    log_info "Creating database backup..."
    if docker exec puppytalk-mysql-prod mysqldump -u root -p"${DB_ROOT_PASSWORD}" "${DB_NAME}" > "${backup_dir}/puppytalk_backup_$(date +%Y%m%d_%H%M%S).sql"; then
        log_success "Database backup created: ${backup_dir}/puppytalk_backup_$(date +%Y%m%d_%H%M%S).sql"
    else
        log_error "Database backup failed"
    fi
    
    # Clean old backups (keep last 7 days)
    log_info "Cleaning old backups (keeping last ${LOG_RETENTION_DAYS} days)..."
    find ./backups -type d -mtime +${LOG_RETENTION_DAYS} -exec rm -rf {} + 2>/dev/null || true
}

# Cleanup operations
cleanup_operations() {
    log_info "Performing cleanup operations..."
    
    # Clean Docker resources
    log_info "Cleaning unused Docker resources..."
    docker system prune -f --filter "until=24h"
    
    # Clean old log files
    log_info "Cleaning old log files..."
    find /var/log -name "*.log" -mtime +${LOG_RETENTION_DAYS} -delete 2>/dev/null || true
    
    log_success "Cleanup operations completed"
}

# Security check
security_check() {
    log_info "Performing basic security checks..."
    
    # Check for running containers with privileged access
    privileged_containers=$(docker ps --filter "label=privileged=true" --quiet)
    if [ -z "$privileged_containers" ]; then
        log_success "No privileged containers detected"
    else
        log_warning "Privileged containers detected - review security"
    fi
    
    # Check for containers running as root
    root_containers=$(docker ps --format "table {{.Names}}\t{{.Image}}" --filter "label=user=root")
    if [ -z "$root_containers" ]; then
        log_success "No containers explicitly running as root"
    else
        log_info "Containers configuration:"
        echo "$root_containers"
    fi
    
    # Check exposed ports
    log_info "Exposed ports:"
    docker ps --format "table {{.Names}}\t{{.Ports}}"
}

# Generate report
generate_report() {
    local report_file="./reports/health_report_$(date +%Y%m%d_%H%M%S).txt"
    mkdir -p ./reports
    
    log_info "Generating health report: $report_file"
    
    {
        echo "PuppyTalk System Health Report"
        echo "Generated: $(date)"
        echo "==============================="
        echo
        
        echo "System Information:"
        uname -a
        echo
        
        echo "Uptime:"
        uptime
        echo
        
        echo "Container Status:"
        docker-compose -f docker-compose.prod.yml ps
        echo
        
        echo "Application Health:"
        curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "Health endpoint not accessible"
        echo
        
        echo "Recent Errors (if any):"
        docker-compose -f docker-compose.prod.yml logs --since="1h" app 2>/dev/null | grep -i "error\|exception\|failed" | tail -5 || echo "No recent errors"
        
    } > "$report_file"
    
    log_success "Health report generated: $report_file"
}

# Main monitoring function
main() {
    log_info "Starting PuppyTalk monitoring process..."
    echo
    
    # Source environment file if exists
    if [[ -f ".env" ]]; then
        source .env
    fi
    
    case "${1:-status}" in
        "health")
            system_health
            application_metrics
            ;;
        "containers")
            container_status
            ;;
        "logs")
            log_analysis
            ;;
        "backup")
            database_maintenance
            ;;
        "cleanup")
            cleanup_operations
            ;;
        "security")
            security_check
            ;;
        "report")
            generate_report
            ;;
        "full"|"status")
            system_health
            application_metrics
            container_status
            log_analysis
            security_check
            generate_report
            ;;
        *)
            echo "Usage: $0 {health|containers|logs|backup|cleanup|security|report|full}"
            echo
            echo "Commands:"
            echo "  health     - System and application health check"
            echo "  containers - Container status and resource usage"
            echo "  logs       - Log analysis and error detection"
            echo "  backup     - Database backup operations"
            echo "  cleanup    - Cleanup old files and Docker resources"
            echo "  security   - Basic security checks"
            echo "  report     - Generate comprehensive health report"
            echo "  full       - Run all monitoring checks (default)"
            exit 1
            ;;
    esac
    
    echo
    log_success "Monitoring completed successfully!"
}

# Error handling
trap 'log_error "Monitoring script failed at line $LINENO"' ERR

# Run main function
main "$@"