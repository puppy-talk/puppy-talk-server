#!/bin/bash

# Docker Configuration Validation Script
set -e

echo "🐶 Puppy Talk Server - Docker Configuration Validation"
echo "======================================================"

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

# Navigate to project root
cd "$(dirname "$0")/.."

# Check Docker and Docker Compose
print_status "Checking Docker installation..."
if ! command -v docker > /dev/null 2>&1; then
    print_error "Docker is not installed"
    exit 1
fi

if ! command -v docker-compose > /dev/null 2>&1; then
    print_error "Docker Compose is not installed"
    exit 1
fi

print_success "Docker and Docker Compose are available"

# Validate Docker Compose files
print_status "Validating Docker Compose configurations..."

print_status "  • Checking main docker-compose.yml..."
if docker-compose config --quiet; then
    print_success "    Main configuration is valid"
else
    print_error "    Main configuration has errors"
    exit 1
fi

print_status "  • Checking development configuration..."
if docker-compose -f docker-compose.yml -f docker-compose.dev.yml config --quiet; then
    print_success "    Development configuration is valid"
else
    print_error "    Development configuration has errors"
    exit 1
fi

print_status "  • Checking production configuration..."
if docker-compose -f docker-compose.yml -f docker-compose.prod.yml config --quiet; then
    print_success "    Production configuration is valid"
else
    print_error "    Production configuration has errors"
    exit 1
fi

# Check required files
print_status "Checking required files..."

required_files=(
    "Dockerfile"
    "Dockerfile.dev"
    ".dockerignore"
    ".env.example"
    "docker-compose.yml"
    "docker-compose.dev.yml"
    "docker-compose.prod.yml"
)

for file in "${required_files[@]}"; do
    if [ -f "$file" ]; then
        print_success "  • $file exists"
    else
        print_error "  • $file is missing"
    fi
done

# Check if .env file exists
if [ -f ".env" ]; then
    print_success "  • .env file exists"
else
    print_warning "  • .env file not found (will be created from .env.example)"
fi

# Validate Dockerfile syntax
print_status "Validating Dockerfiles..."

if command -v hadolint > /dev/null 2>&1; then
    print_status "  • Running Hadolint on Dockerfiles..."
    hadolint Dockerfile && print_success "    Dockerfile is well-formed"
    hadolint Dockerfile.dev && print_success "    Dockerfile.dev is well-formed"
else
    print_warning "  • Hadolint not available (install for Dockerfile linting)"
fi

# Check port availability
print_status "Checking port availability..."

ports=(8080 3306 6379 9090 3000 8081 8082 11434)
for port in "${ports[@]}"; do
    if netstat -tuln 2>/dev/null | grep -q ":$port "; then
        print_warning "  • Port $port is in use"
    else
        print_success "  • Port $port is available"
    fi
done

# Summary
print_success "Docker configuration validation completed!"
echo ""
echo "📋 Next steps:"
echo "   1. Create .env file: cp .env.example .env"
echo "   2. Modify .env file with your settings"
echo "   3. Run: ./scripts/build.sh"
echo ""
echo "🚀 Available commands:"
echo "   • Development: docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d"
echo "   • Production:  docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d"
echo "   • Basic:       docker-compose up -d"