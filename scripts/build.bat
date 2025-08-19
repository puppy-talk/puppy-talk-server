@echo off
setlocal enabledelayedexpansion

echo 🐶 Puppy Talk Server - Docker Build Script (Windows)
echo ===================================================

REM Check if Docker is running
docker info >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Docker is not running. Please start Docker and try again.
    exit /b 1
)

REM Check if docker-compose is available
docker-compose --version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] docker-compose is not installed. Please install docker-compose and try again.
    exit /b 1
)

REM Navigate to project root
cd /d "%~dp0\.."

echo [INFO] Building Puppy Talk Server with Docker...

REM Create .env file if it doesn't exist
if not exist .env (
    echo [WARNING] .env file not found. Creating from .env.example...
    copy .env.example .env >nul
    echo [SUCCESS] .env file created. Please review and modify as needed.
)

REM Clean up existing containers
echo [INFO] Cleaning up existing containers...
docker-compose down --volumes --remove-orphans 2>nul

REM Build and start services
echo [INFO] Building and starting services...
docker-compose build --no-cache
docker-compose up -d

REM Wait for services
echo [INFO] Waiting for services to be ready...
timeout /t 30 >nul

echo [INFO] Checking service health...

REM Check services
curl -f http://localhost:8080/actuator/health >nul 2>&1
if !errorlevel! equ 0 (
    echo [SUCCESS] Application is healthy
) else (
    echo [WARNING] Application might still be starting up...
)

echo.
echo [SUCCESS] Build completed successfully!
echo.
echo 🚀 Services are running:
echo    • Application: http://localhost:8080
echo    • Adminer (DB): http://localhost:8081
echo    • Redis Commander: http://localhost:8082
echo    • Grafana: http://localhost:3000
echo    • Prometheus: http://localhost:9090
echo.
echo 📋 Useful commands:
echo    • View logs: docker-compose logs -f
echo    • Stop services: docker-compose down
echo    • Restart: docker-compose restart
echo.
echo [SUCCESS] 🐶 Puppy Talk Server is ready!

pause