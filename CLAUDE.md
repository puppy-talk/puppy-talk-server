# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 개요

Java Spring Boot 멀티모듈 프로젝트로 **Hexagonal Architecture (Ports and Adapters Architecture)** 패턴을 기반으로 합니다. Puppy Talk 서버는 Pet 도메인을 중심으로 한 RESTful API를 제공합니다.

## 주요 개발 명령어

### 빌드 및 실행
```bash
# 전체 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행 (local 프로필)
./gradlew application-api:bootRun

# 특정 모듈 빌드
./gradlew {module-name}:build

# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew {module-name}:test
```

### Docker 환경
```bash
# 개발용: MySQL만 Docker로 실행
docker-compose -f docker-compose.dev.yml up -d

# 전체 서비스 실행 (권장 방법)
./gradlew application-api:bootJar
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 환경 정리
docker-compose down -v
```

### 빠른 스크립트 실행
```bash
# scripts/build.sh 사용 (MacOS/Linux)
cd scripts && sh build.sh
```

## Hexagonal Architecture 구조

### 핵심 아키텍처 원칙

1. **의존성 역전**: Service는 Infrastructure 구현체가 아닌 인터페이스에 의존
2. **단방향 의존**: 상위 레이어는 하위 레이어를 의존하지만 역방향 불가
3. **순수 도메인**: Model과 Exception은 외부 의존성 없음
4. **포트와 어댑터**: Infrastructure 모듈은 포트(인터페이스)만 정의, 구현은 별도 모듈

### 레이어별 역할

**Domain Layer (핵심 비즈니스)**
- `model/`: 도메인 엔티티 (Pet, PetIdentity) - 외부 의존성 없음
- `exception/`: 도메인 예외 (PetNotFoundException) - model 모듈만 의존

**Application Layer (비즈니스 로직)**
- `service/`: 유스케이스 구현 (PetLookUpService) - Infrastructure 포트를 통해 외부 시스템과 통신
- `infrastructure/`: 포트 정의 (PetRepository 인터페이스) - 구현체는 포함하지 않음

**Adapter Layer (외부 시스템 연동)**
- `api/`: HTTP 어댑터 (PetController, PetResponse) - Service에 의존
- `repository-jdbc/`: JDBC 어댑터 (PetJdbcRepository) - Infrastructure 포트 구현

**Infrastructure Layer**
- `application-api/`: 스프링 부트 애플리케이션 구성 및 의존성 와이어링
- `schema/`: Liquibase를 통한 데이터베이스 스키마 관리

### 의존성 흐름
```
api → service → infrastructure ← repository-jdbc
              ↘ model ↙
              ↘ exception
```

## 기술적 특징

### 스프링 프로필 관리
- **local**: 로컬 MySQL (localhost:3306) - 환경변수 지원
- **docker**: Docker 컨테이너 간 연결 (mysql:3306)
- **test**: H2 인메모리 데이터베이스

### 데이터베이스 스키마 관리
- Liquibase 사용 (`schema/src/main/resources/db/changelog/`)
- 상대 경로 및 논리적 파일 경로 설정으로 이식성 확보
- 변경 로그는 XML 형식으로 관리

### ID 생성 전략
- PetJdbcRepository에서 KeyHolder 사용하여 자동 생성 ID 처리
- 삽입 시 생성된 ID로 새로운 Pet 객체 반환

    

### 모듈 간 의존성 규칙
- Domain 모듈(model, exception)은 외부 의존성 금지
- Service는 Infrastructure 인터페이스만 의존, 구현체 직접 의존 금지  
- Repository 구현체는 Infrastructure 인터페이스 구현
- API 레이어는 Service와 Exception에만 의존

### 코드 작성 원칙
- DTO는 record 타입 사용
- Service 메서드에는 @Transactional(readOnly = true) 적용 (조회)
- 생성자에서 null 및 유효성 검증 수행
- Builder 패턴 사용 시에도 동일한 검증 로직 적용

### Spring Security 금지
- 프로젝트 정책에 따라 Spring Security 사용 금지
- 인증/인가가 필요한 경우 대안 방안 검토 필요

### 컨벤션
- 패키지: `com.puppy.talk.{domain}.{layer}`
- Service: `{Domain}LookUpService`
- Repository: `{Domain}Repository` (인터페이스), `{Domain}JdbcRepository` (구현체)
- Identity: `{Domain}Identity`

## 환경 설정

### 환경변수 (local 프로필)
- `SPRING_DATASOURCE_URL`: MySQL 연결 URL (기본값: jdbc:mysql://localhost:3306/puppy_talk_db?useSSL=false&allowPublicKeyRetrieval=true)
- `SPRING_DATASOURCE_USERNAME`: DB 사용자명 (기본값: root)  
- `SPRING_DATASOURCE_PASSWORD`: DB 패스워드 (기본값: 1234)

### API 문서화
- SpringDoc OpenAPI 사용
- `/swagger-ui.html`에서 API 문서 확인 가능

### 컨테이너 최적화
- Amazon Corretto 21 JRE 기반
- Non-root 사용자로 실행 (보안 강화)
- G1GC 및 컨테이너 최적화 JVM 옵션 적용