#!/bin/bash

# Puppy Talk Server - Docker Build Script
set -e

echo "🐶 Puppy Talk Server - Docker Build Script"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose > /dev/null 2>&1; then
    print_error "docker-compose is not installed. Please install docker-compose and try again."
    exit 1
fi

# Navigate to project root
cd "$(dirname "$0")/.."

print_status "Building Puppy Talk Server with Docker..."

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    print_warning ".env file not found. Creating from .env.example..."
    cp .env.example .env
    print_success ".env file created. Please review and modify as needed."
fi

# Clean up existing containers and images
print_status "Cleaning up existing containers..."
docker-compose down --volumes --remove-orphans || true

# Build and start services
print_status "Building and starting services..."
docker-compose build --no-cache
docker-compose up -d

# Wait for services to be ready
print_status "Waiting for services to be ready..."
sleep 30

# Check service health
print_status "Checking service health..."

# Check MySQL
if docker-compose exec -T mysql mysqladmin ping -h localhost --silent; then
    print_success "MySQL is healthy"
else
    print_error "MySQL is not healthy"
fi

# Check Redis
if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
    print_success "Redis is healthy"
else
    print_error "Redis is not healthy"
fi

# Check Application
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    print_success "Application is healthy"
else
    print_warning "Application might still be starting up..."
fi

print_success "Build completed successfully!"
echo ""
echo "🚀 Services are running:"
echo "   • Application: http://localhost:8080"
echo "   • Adminer (DB): http://localhost:8081"
echo "   • Redis Commander: http://localhost:8082"
echo "   • Grafana: http://localhost:3000"
echo "   • Prometheus: http://localhost:9090"
echo ""
echo "📋 Useful commands:"
echo "   • View logs: docker-compose logs -f"
echo "   • Stop services: docker-compose down"
echo "   • Restart: docker-compose restart"
echo ""
print_success "🐶 Puppy Talk Server is ready!"