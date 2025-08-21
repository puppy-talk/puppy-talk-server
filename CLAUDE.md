# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Puppy Talk Server is a Java Spring Boot application that provides a chat platform for users to interact with AI-powered pet personas. The system uses a layered architecture with clear separation of concerns.

## Development Commands

### Build & Run
```bash
# Build and run with Docker
cd scripts && sh build.sh

# Build all modules
./gradlew buildAll

# Run application locally
./gradlew :application-api:bootRun

# Clean all modules
./gradlew cleanAll
```

### Testing
```bash
# Run all tests
./gradlew testAll

# Run tests for specific module
./gradlew :model:test
./gradlew :ai-service:test
./gradlew :auth-service:test

# Run with code coverage
./gradlew jacocoAll
```

### Quality & Analysis
```bash
# Run SonarQube analysis
./gradlew sonarqube

# Generate test coverage reports
./gradlew jacocoTestReport
```

## Architecture Overview

### Layered Architecture (Top-Down Dependencies)
1. **Application Layer** (`application-api`) - Spring Boot bootstrap, configuration
2. **Presentation Layer** (`api`) - REST controllers, WebSocket handlers, DTOs
3. **Application Logic Layer** (`application`) - Facade pattern, use case orchestration
4. **Business Logic Layer** (`domain`) - Domain services, business rules and logic
5. **Infrastructure Layer** (`infrastructure`) - Repository and external service interfaces (ports)
6. **Data Access Layer** (`repository-jdbc`) - JDBC implementations of repository interfaces
7. **Domain Model Layer** (`model`) - Core domain entities and value objects
8. **Exception Layer** (`exception`) - Business exceptions and error handling
9. **Schema Layer** (`schema`) - Database migrations and schema management
10. **External Services** (`ai-service`) - Multi-provider AI integration

### Key Architectural Principles
- **Hexagonal Architecture**: External integrations (AI, push notifications) use Port-Adapter pattern
- **Repository Pattern**: Data access abstracted through interfaces in `infrastructure` module
- **Dependency Inversion**: Business logic depends on abstractions, not implementations
- **Single Responsibility**: Each module has a clearly defined purpose

## Module Structure

### Core Domain Modules
- `model` - Domain entities (Pet, User, ChatRoom, Message, etc.)
- `exception` - Business exceptions
- `infrastructure` - Repository and external service interfaces (ports)
- `repository-jdbc` - JDBC implementations of repository interfaces

### Business Logic Modules
- `domain` - Domain services, business rules, and domain logic implementations:
  - Authentication services (`auth/`)
  - Chat services (`chat/`)
  - Pet management services (`pet/`)
  - User management services (`user/`) 
  - Notification services (`notification/`)
- `application` - Application-level facades and use case orchestration:
  - Chat application service
  - Pet facade
  - Notification scheduling and services
  - WebSocket configuration

### External Integration Modules
- `ai-service` - Multi-provider AI integration (OpenAI, Claude, Gemini, local Ollama)

### Application Modules  
- `api` - REST controllers and WebSocket handlers
- `application-api` - Spring Boot main application
- `schema` - Liquibase database migrations

## Key Technologies

### Framework Stack
- Java 21
- Spring Boot 3.4.0
- Spring Framework 6.1.0
- Spring WebSocket for real-time communication

### Database & Persistence
- MySQL 8.0 (production/docker)
- MariaDB (local development)
- H2 (testing)
- Liquibase for schema migrations
- HikariCP connection pooling

### External Integrations
- Multiple AI providers: OpenAI, Anthropic Claude, Google Gemini, local Ollama
- Firebase Cloud Messaging for push notifications
- Redis for caching and session management

### Testing
- JUnit 5
- AssertJ
- Mockito
- Testcontainers for integration tests

## Configuration Profiles

### Environment Profiles
- `local` - Local development with MariaDB
- `docker` - Docker environment with MySQL
- `test` - Testing with H2 in-memory database

### AI Provider Configuration
The system supports multiple AI providers with fallback capabilities:
- Primary: `gpt-oss` (local Ollama instance)
- Fallback: OpenAI, Claude, Gemini (requires API keys)

## Development Guidelines

### Module Dependencies
Follow strict layered architecture - higher layers can depend on lower layers only:
```
application-api → api → application → domain → infrastructure → repository-jdbc → model
                     ↘ ai-service
```

Current modules and their typical dependencies:
- `application-api` depends on: `api`, `application`, `domain`, `infrastructure`
- `api` depends on: `application`, `domain`, `model`, `exception`
- `application` depends on: `domain`, `infrastructure`, `model`, `exception`  
- `domain` depends on: `infrastructure`, `model`, `exception`
- `infrastructure` depends on: `model`, `exception`
- `repository-jdbc` depends on: `infrastructure`, `model`, `exception`
- `ai-service` depends on: `model`, `exception`

### Testing Strategy
- Unit tests for business logic in `domain` modules
- Integration tests using Testcontainers for `repository-jdbc` implementations
- Application service tests in `application` module
- WebSocket tests for real-time functionality in `api` module

### Database Changes
- All schema changes must be done through Liquibase migrations in `schema` module
- Migration files follow naming convention: `XXX-description.xml`

### AI Integration
- New AI providers should implement `AiProvider` interface
- Add provider configuration to `application.yml`
- Provider factory automatically selects available providers

### WebSocket Communication
- Real-time chat uses STOMP protocol over WebSocket
- Authentication handled via JWT tokens in WebSocket headers
- Message broadcasting managed by services in `infrastructure` module
- WebSocket configuration and controllers in `api` and `application` modules

## Docker Environment

### Services
- `puppy-talk-app` - Main Spring Boot application
- `mysql` - Database
- `redis` - Caching and session store
- `ollama` - Local AI model server
- `nginx` - Load balancer and reverse proxy
- `prometheus` + `grafana` - Monitoring stack
- `adminer` - Database administration (dev profile)
- `redis-commander` - Redis administration (dev profile)

### Ports
- Application: 8080
- Database: 3306
- Redis: 6379
- Ollama: 11434
- Nginx: 80, 443
- Monitoring: Grafana (3000), Prometheus (9090)
- Admin tools: Adminer (8081), Redis Commander (8082)

## Important Notes

### Security
- JWT tokens for authentication with configurable expiration
- BCrypt password encoding
- Firebase service account key required for push notifications

### Performance
- Connection pooling with HikariCP
- Redis caching for session management
- Async processing for AI responses and notifications

### Monitoring
- Actuator endpoints for health checks
- Prometheus metrics collection
- Grafana dashboards for visualization

## Recent Architectural Changes

### Current Project State
The project is undergoing a major architectural refactoring from a monolithic service structure to a clean layered architecture:

**Completed Migrations:**
- Extracted `domain` module with business logic and domain services
- Created `application` module for facade pattern and use case orchestration
- Consolidated authentication, chat, pet, user, and notification services into `domain` module
- Maintained `ai-service` as external integration module

**Legacy Modules Removed:**
- Individual service modules (`auth-service`, `chat-service`, `pet-service`, etc.) have been consolidated
- Service-specific classes moved to appropriate domain packages within `domain` module

**Current Module Structure:** 
The current `settings.gradle` shows the active modules, with legacy modules commented out or removed.

### Important Notes for Development

**Architecture Compliance:**
- Follow the layered dependency rules strictly  
- Business logic belongs in `domain` module, not in `application` facades
- `application` module should only orchestrate domain services and handle transactions
- Repository interfaces in `infrastructure`, implementations in `repository-jdbc`

**Build and Testing:**
- Use `./gradlew buildAll` to build all modules
- Use `./gradlew testAll` to run all tests  
- Individual module tests: `./gradlew :module-name:test`
- Code coverage: `./gradlew jacocoAll`

This architecture provides a scalable, maintainable foundation that can evolve toward microservices as the system grows.