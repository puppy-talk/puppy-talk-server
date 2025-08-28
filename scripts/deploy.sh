#!/bin/bash

# PuppyTalk Production Deployment Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="puppytalk-server"
DOCKER_IMAGE="puppytalk:latest"
ENV_FILE=".env"

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

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    # Check environment file
    if [[ ! -f "$ENV_FILE" ]]; then
        log_error "Environment file '$ENV_FILE' not found"
        log_info "Please copy .env.example to .env and configure your environment variables"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Validate environment
validate_environment() {
    log_info "Validating environment configuration..."
    
    # Source environment file
    source "$ENV_FILE"
    
    # Check required variables
    required_vars=("DB_NAME" "DB_USERNAME" "DB_PASSWORD" "DB_ROOT_PASSWORD")
    
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            log_error "Required environment variable '$var' is not set"
            exit 1
        fi
    done
    
    # Check for default/example values
    if [[ "$DB_PASSWORD" == "your_secure_password_here" ]] || [[ "$DB_ROOT_PASSWORD" == "your_root_password_here" ]]; then
        log_error "Please update default password values in $ENV_FILE"
        exit 1
    fi
    
    log_success "Environment validation passed"
}

# Run tests
run_tests() {
    log_info "Running tests..."
    
    if ./gradlew test; then
        log_success "All tests passed"
    else
        log_error "Tests failed"
        exit 1
    fi
}

# Build application
build_application() {
    log_info "Building application..."
    
    if ./gradlew clean build; then
        log_success "Application built successfully"
    else
        log_error "Build failed"
        exit 1
    fi
}

# Build Docker image
build_docker_image() {
    log_info "Building Docker image..."
    
    if docker build -t "$DOCKER_IMAGE" .; then
        log_success "Docker image built successfully"
    else
        log_error "Docker build failed"
        exit 1
    fi
}

# Deploy with Docker Compose
deploy_application() {
    log_info "Deploying application..."
    
    # Stop existing containers
    log_info "Stopping existing containers..."
    docker-compose -f docker-compose.prod.yml down --remove-orphans
    
    # Start new containers
    log_info "Starting containers..."
    if docker-compose -f docker-compose.prod.yml up -d; then
        log_success "Application deployed successfully"
    else
        log_error "Deployment failed"
        exit 1
    fi
}

# Health check
health_check() {
    log_info "Performing health check..."
    
    # Wait for application to start
    sleep 30
    
    # Check application health
    max_attempts=10
    attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
            log_success "Health check passed"
            return 0
        fi
        
        log_info "Health check attempt $attempt/$max_attempts failed, retrying in 10 seconds..."
        sleep 10
        ((attempt++))
    done
    
    log_error "Health check failed after $max_attempts attempts"
    return 1
}

# Show deployment status
show_status() {
    log_info "Deployment Status:"
    echo
    
    # Show container status
    docker-compose -f docker-compose.prod.yml ps
    
    echo
    log_info "Application URL: http://localhost:8080"
    log_info "API Documentation: http://localhost:8080/swagger-ui/index.html"
    log_info "Health Check: http://localhost:8080/actuator/health"
    
    echo
    log_info "To view logs:"
    log_info "  docker-compose -f docker-compose.prod.yml logs -f app"
    
    echo
    log_info "To stop the application:"
    log_info "  docker-compose -f docker-compose.prod.yml down"
}

# Rollback function
rollback() {
    log_warning "Performing rollback..."
    
    # Stop current containers
    docker-compose -f docker-compose.prod.yml down --remove-orphans
    
    log_info "Rollback completed. Please check the previous deployment."
}

# Cleanup function
cleanup() {
    log_info "Cleaning up..."
    
    # Remove unused Docker images
    docker image prune -f
    
    # Remove unused containers
    docker container prune -f
    
    log_success "Cleanup completed"
}

# Main deployment process
main() {
    log_info "Starting PuppyTalk deployment process..."
    echo
    
    case "${1:-deploy}" in
        "deploy")
            check_prerequisites
            validate_environment
            run_tests
            build_application
            build_docker_image
            deploy_application
            health_check
            show_status
            log_success "Deployment completed successfully!"
            ;;
        "rollback")
            rollback
            ;;
        "cleanup")
            cleanup
            ;;
        "status")
            show_status
            ;;
        "health")
            health_check
            ;;
        *)
            echo "Usage: $0 {deploy|rollback|cleanup|status|health}"
            echo
            echo "Commands:"
            echo "  deploy   - Full deployment process (default)"
            echo "  rollback - Rollback to previous deployment"
            echo "  cleanup  - Clean up unused Docker resources"
            echo "  status   - Show deployment status"
            echo "  health   - Perform health check only"
            exit 1
            ;;
    esac
}

# Error handling
trap 'log_error "Script failed at line $LINENO"' ERR

# Run main function
main "$@"