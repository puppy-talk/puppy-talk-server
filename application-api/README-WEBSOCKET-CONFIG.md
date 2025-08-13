# WebSocket Configuration Guide

## Overview
이 문서는 Puppy Talk 서버의 WebSocket 설정을 환경별로 구성하는 방법을 설명합니다.

## Environment-Specific Configuration

### Local Development
- **Profile**: `local`
- **File**: `application.yml`
- **WebSocket Origins**: `http://localhost:3000,http://localhost:8080`
- **Security**: 모든 Origin 허용 (개발 편의성)

### Staging Environment
- **Profile**: `staging`
- **File**: `application-staging.yml`
- **WebSocket Origins**: `https://staging.example.com,https://staging-api.example.com`
- **Security**: HTTPS 도메인만 허용

### Production Environment
- **Profile**: `prod`
- **File**: `application-prod.yml`
- **WebSocket Origins**: `https://example.com,https://api.example.com,https://app.example.com`
- **Security**: HTTPS 도메인만 허용, 엄격한 보안 설정

## Configuration Properties

### Required Properties
```yaml
puppy-talk:
  websocket:
    allowed-origins: [HTTPS_DOMAINS]
```

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: 활성 프로필 설정
- `PUPPY_TALK_WEBSOCKET_ALLOWED_ORIGINS`: WebSocket 허용 Origin 오버라이드

## Security Considerations

### Development vs Production
- **Development**: `setAllowedOriginPatterns("*")` - 모든 Origin 허용
- **Production**: `setAllowedOrigins(allowedOrigins)` - 설정된 Origin만 허용

### HTTPS Enforcement
- 프로덕션 환경에서는 반드시 HTTPS 도메인만 허용
- HTTP 도메인은 보안상 허용하지 않음

## Deployment

### Local Development
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Staging Deployment
```bash
./gradlew bootRun --args='--spring.profiles.active=staging'
```

### Production Deployment
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Troubleshooting

### Common Issues
1. **Missing Configuration**: `puppy-talk.websocket.allowed-origins` 속성이 설정되지 않은 경우
2. **Invalid Origins**: HTTPS가 아닌 도메인을 프로덕션에서 사용하려는 경우
3. **Profile Mismatch**: 활성 프로필과 설정 파일이 일치하지 않는 경우

### Validation
- 애플리케이션 시작 시 설정된 Origin 로그 확인
- WebSocket 연결 시 Origin 검증 로그 확인
