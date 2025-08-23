# PuppyTalk Server

퍼피톡은 생성형 AI 기반의 반려동물 채팅 서비스입니다.

## 🐶 프로젝트 개요

### 주요 기능
- **반려동물 생성**: 사용자가 선택한 페르소나를 기반으로 반려동물 생성
- **AI 채팅**: 생성된 반려동물과의 실시간 대화
- **스마트 알림**: 사용자 비활성 시간을 기반으로 한 개인화된 알림

### 기술 스택
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Build Tool**: Gradle 8.x
- **Database**: MySQL 8.0 (Production), H2 (Test)
- **Architecture**: Clean Architecture (4-Layer)
- **Containerization**: Docker & Docker Compose

## 🏗️ 아키텍처

### Clean Architecture 4-Layer 구조
```
puppytalk-server/
├── puppytalk-api/           # 프레젠테이션 레이어 (REST API)
├── puppytalk-application/   # 애플리케이션 레이어 (Facade)
├── puppytalk-domain/        # 도메인 레이어 (비즈니스 로직)
├── puppytalk-infrastructure/# 인프라스트럭처 레이어 (데이터 액세스)
├── puppytalk-bootstrap/     # 부트스트랩 레이어 (구성 및 실행)
└── puppytalk-test/         # 통합 테스트
```

### 도메인 모델
- **User**: 사용자 관리 (회원가입, 인증, 프로필)
- **Pet**: 반려동물 생성 및 페르소나 관리
- **Chat**: 반려동물과의 대화 관리
- **Activity**: 사용자 활동 추적
- **Notification**: 알림 생성 및 발송

## 🚀 빠른 시작

### 개발 환경 설정

1. **필수 요구사항**
   ```bash
   - Java 17+
   - Docker & Docker Compose
   - Git
   ```

2. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd puppy-talk-server
   ```

3. **환경 변수 설정**
   ```bash
   cp .env.example .env
   # .env 파일을 편집하여 환경에 맞게 구성
   ```

4. **개발 서버 실행**
   ```bash
   ./gradlew bootRun
   ```

### 프로덕션 배포

1. **배포 스크립트 실행**
   ```bash
   chmod +x scripts/deploy.sh
   ./scripts/deploy.sh
   ```

2. **서비스 확인**
   - API: http://localhost:8080
   - API 문서: http://localhost:8080/swagger-ui/index.html
   - 헬스체크: http://localhost:8080/actuator/health

## 🛠️ 개발 가이드

### 코딩 규칙

1. **Modern Java 활용**
   - Record 적극 활용 (불변성)
   - Stream API 및 함수형 인터페이스
   - Optional 활용

2. **도메인 모델**
   - 정적 팩토리 메서드 패턴
   - 생성자 private 선언
   - 상태와 행위를 함께 보유

3. **예외 처리**
   - GlobalExceptionHandler 활용
   - try-catch 사용 지양

### API 설계

- **메서드명 규칙**: `{domain}List` (목록), `{domain}` (단건)
- **REST 원칙**: 적절한 HTTP 메서드 및 상태 코드
- **응답 형식**: 통일된 ApiResponse 구조

### 테스트 전략

- **Mockito 사용 금지**: 실제 객체 기반 테스트
- **통합 테스트**: 전체 시나리오 검증
- **아키텍처 테스트**: ArchUnit 활용

## 🔧 운영 가이드

### 모니터링

```bash
# 전체 시스템 상태 확인
./scripts/monitoring.sh

# 특정 항목 확인
./scripts/monitoring.sh health      # 헬스체크
./scripts/monitoring.sh containers  # 컨테이너 상태
./scripts/monitoring.sh logs       # 로그 분석
./scripts/monitoring.sh backup     # 데이터베이스 백업
```

### 로그 확인

```bash
# 실시간 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app

# 에러 로그 확인
docker-compose -f docker-compose.prod.yml logs app | grep -i error
```

### 데이터베이스 관리

```bash
# 백업 생성
./scripts/monitoring.sh backup

# 마이그레이션 실행
./gradlew flywayMigrate
```

## 📊 성능 및 모니터링

### 헬스체크 엔드포인트
- `/actuator/health`: 서비스 상태
- `/actuator/info`: 애플리케이션 정보
- `/actuator/metrics`: 메트릭 정보

### 리소스 제한
- **애플리케이션**: 메모리 1.5GB 제한
- **데이터베이스**: 메모리 1GB 제한
- **자동 스케일링**: Docker Compose 리소스 관리

## 🔒 보안

### 기본 보안 설정
- 비root 사용자로 컨테이너 실행
- 최소 권한 원칙 적용
- 환경 변수를 통한 보안 정보 관리

### 인증 및 권한
- JWT 기반 인증 (구성 예정)
- API 엔드포인트 보안
- 입력 검증 및 SQL 인젝션 방지

## 🤝 기여 가이드

1. **브랜치 전략**: feature/기능명
2. **커밋 규칙**: 
   - feat: 새로운 기능
   - fix: 버그 수정
   - refactor: 리팩토링
   - docs: 문서 수정

3. **코드 리뷰**: PR을 통한 코드 리뷰 필수

## 📋 API 문서

서버 실행 후 다음 주소에서 API 문서 확인:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🐛 문제 해결

### 일반적인 문제

1. **빌드 실패**
   ```bash
   ./gradlew clean build
   ```

2. **데이터베이스 연결 실패**
   - .env 파일의 데이터베이스 설정 확인
   - Docker 컨테이너 상태 확인

3. **포트 충돌**
   ```bash
   # 포트 사용 중인 프로세스 확인
   lsof -i :8080
   ```

### 로그 레벨 조정

```yaml
# application.yml
logging:
  level:
    com.puppytalk: DEBUG
    org.springframework.web: DEBUG
```

## 📞 지원

- **이슈 리포팅**: GitHub Issues
- **문의사항**: 프로젝트 관리자에게 연락

---

**PuppyTalk Server** - AI 기반 반려동물 채팅 서비스 🐕💬