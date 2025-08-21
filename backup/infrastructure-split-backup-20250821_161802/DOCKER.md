# 🐶 Puppy Talk Server - Docker Setup Guide

This guide explains how to run the Puppy Talk Server using Docker and Docker Compose.

## 📋 Prerequisites

- Docker 20.10+ and Docker Compose 2.0+
- At least 4GB RAM available for containers
- Ports 8080, 3306, 6379, 9090, 3000 available

## 🚀 Quick Start

### 1. Clone and Setup
```bash
git clone <repository-url>
cd puppy-talk-server
cp .env.example .env
```

### 2. Build and Run
```bash
# macOS/Linux
cd scripts && ./build.sh

# Windows
cd scripts && build.bat
```

### 3. Access Services
- **Application**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Admin**: http://localhost:8081 (Adminer)
- **Redis Admin**: http://localhost:8082 (Redis Commander)
- **Monitoring**: http://localhost:3000 (Grafana)
- **Metrics**: http://localhost:9090 (Prometheus)

## 🏗️ Architecture

### Services Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Nginx       │    │   Puppy Talk    │    │     MySQL       │
│  Load Balancer  │────│   Application   │────│    Database     │
│    (Port 80)    │    │   (Port 8080)   │    │   (Port 3306)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                       ┌─────────────────┐    ┌─────────────────┐
                       │      Redis      │    │     Ollama      │
                       │     Cache       │    │   AI Service    │
                       │   (Port 6379)   │    │  (Port 11434)   │
                       └─────────────────┘    └─────────────────┘
```

### Container Details

| Service | Image | Purpose | Ports |
|---------|-------|---------|-------|
| puppy-talk-app | Custom (Java 21) | Main application | 8080 |
| mysql | mysql:8.0 | Primary database | 3306 |
| redis | redis:7-alpine | Caching & sessions | 6379 |
| nginx | nginx:alpine | Load balancer | 80, 443 |
| ollama | ollama/ollama | Local AI inference | 11434 |
| prometheus | prom/prometheus | Metrics collection | 9090 |
| grafana | grafana/grafana | Monitoring dashboard | 3000 |
| adminer | adminer:latest | DB management | 8081 |
| redis-commander | rediscommander/redis-commander | Redis management | 8082 |

## 🛠️ Configuration

### Environment Variables (.env)

```bash
# Database
MYSQL_ROOT_PASSWORD=1234
MYSQL_DATABASE=puppy_talk_db
MYSQL_USER=puppy_user
MYSQL_PASSWORD=1234

# Security
JWT_SECRET=your-super-secret-jwt-key

# AI Configuration
AI_DEFAULT_PROVIDER=gpt-oss
AI_GPT_OSS_SERVER_URL=http://ollama:11434

# Firebase (Optional)
FIREBASE_SERVICE_ACCOUNT_KEY=firebase-service-account.json
FIREBASE_PROJECT_ID=puppy-talk-dev
```

### Application Profiles

- **docker**: Production-like environment
- **docker,dev**: Development with debug logging
- **docker,prod**: Production optimizations

## 📖 Usage Examples

### Development Mode
```bash
# Start with development tools
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# View application logs
docker-compose logs -f puppy-talk-app

# Connect to database
docker-compose exec mysql mysql -u root -p puppy_talk_db
```

### Production Mode
```bash
# Deploy to production
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Scale application instances
docker-compose up -d --scale puppy-talk-app=3
```

### Monitoring
```bash
# Check all service status
docker-compose ps

# View resource usage
docker stats

# Check logs for specific service
docker-compose logs -f mysql
```

## 🔧 Common Commands

### Container Management
```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart specific service
docker-compose restart puppy-talk-app

# Rebuild and restart
docker-compose up -d --build

# Clean everything (including volumes)
docker-compose down -v --remove-orphans
```

### Database Operations
```bash
# Create database backup
docker-compose exec mysql mysqldump -u root -p puppy_talk_db > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -p puppy_talk_db < backup.sql

# Access MySQL shell
docker-compose exec mysql mysql -u root -p
```

### Application Management
```bash
# View application logs
docker-compose logs -f puppy-talk-app

# Execute command in app container
docker-compose exec puppy-talk-app /bin/bash

# Update application without downtime
docker-compose build puppy-talk-app
docker-compose up -d --no-deps puppy-talk-app
```

## 🚨 Troubleshooting

### Common Issues

**Port conflicts**
```bash
# Check which service is using the port
lsof -i :8080
# or
netstat -tulpn | grep 8080
```

**Database connection issues**
```bash
# Check MySQL status
docker-compose exec mysql mysqladmin ping -h localhost

# Verify database exists
docker-compose exec mysql mysql -u root -p -e "SHOW DATABASES;"
```

**Application startup problems**
```bash
# Check application logs
docker-compose logs puppy-talk-app

# Verify Java process
docker-compose exec puppy-talk-app ps aux
```

### Health Checks

All services include health checks. Check status:
```bash
docker-compose ps
```

Healthy services show "healthy" status. If unhealthy:
1. Check logs: `docker-compose logs [service-name]`
2. Verify dependencies are running
3. Check configuration and environment variables

### Performance Tuning

**Memory Issues**
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g"
docker-compose up -d --build puppy-talk-app
```

**Database Performance**
```bash
# Optimize MySQL configuration
# Edit docker-compose.yml mysql command section
```

## 🔐 Security Considerations

### Production Checklist
- [ ] Change default passwords in `.env`
- [ ] Use strong JWT secret
- [ ] Configure SSL certificates for Nginx
- [ ] Enable firewall rules
- [ ] Regular security updates
- [ ] Backup strategy implementation

### SSL Configuration
Place certificates in `nginx/ssl/` directory:
- `cert.pem` - SSL certificate
- `key.pem` - Private key

## 📊 Monitoring & Metrics

### Grafana Dashboard
1. Access http://localhost:3000
2. Login: admin/admin (change password)
3. Import dashboard from `monitoring/dashboards/`

### Prometheus Metrics
- Application metrics: http://localhost:8080/actuator/prometheus
- Prometheus UI: http://localhost:9090

### Health Endpoints
- Application health: http://localhost:8080/actuator/health
- Detailed health: http://localhost:8080/actuator/health/detail

## 📝 Development Tips

### Hot Reload Development
```bash
# Use development compose file
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Application will auto-restart on code changes
```

### Database Development
```bash
# Access Adminer for DB management
# URL: http://localhost:8081
# Server: mysql
# Username: root or puppy_user
# Password: (from .env file)
# Database: puppy_talk_db
```

### Testing
```bash
# Run tests inside container
docker-compose exec puppy-talk-app ./gradlew test

# Run with specific profile
docker-compose exec puppy-talk-app ./gradlew test -Dspring.profiles.active=test
```

## 📚 Additional Resources

- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
- [Redis Docker Hub](https://hub.docker.com/_/redis)