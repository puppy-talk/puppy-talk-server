# 퍼피톡 로깅 시스템 가이드

## 개요

퍼피톡 서버는 구조화된 로깅 시스템을 사용하여 애플리케이션의 동작을 추적하고 모니터링합니다.

## 로깅 구조

### 1. 전역 로깅 설정

- **위치**: `puppytalk-shared/src/main/resources/logback-spring.xml`
- **MDC 필터**: `puppytalk-shared/src/main/java/com/puppytalk/shared/logging/LoggingFilter.java`

#### 환경별 설정

**개발 환경 (local, dev, docker)**
- 컬러 로그 출력
- 콘솔에 사람이 읽기 쉬운 형태로 출력
- MDC 정보 포함 (`requestId`, `userId`)

**운영 환경 (prod)**
- JSON 구조화 로그
- 파일 저장 (`logs/puppytalk.log`)
- 에러 로그 별도 저장 (`logs/puppytalk-error.log`)
- 로그 로테이션 및 압축

### 2. 스케줄러 전용 로깅

- **위치**: `puppytalk-scheduler/src/main/java/com/puppytalk/scheduler/LogFormats.java`
- **목적**: 배치 작업의 시작/완료/실패를 구조화된 형태로 기록

#### 주요 로그 포맷

```java
// 스케줄러 공통
SCHEDULER_START = "SCHEDULER_START: jobName={}, startTime={}"
SCHEDULER_COMPLETE = "SCHEDULER_COMPLETE: jobName={}, duration={}ms, processedCount={}"
SCHEDULER_ERROR = "SCHEDULER_ERROR: jobName={}, error={}, duration={}ms"

// 휴면 사용자 처리
DORMANT_PROCESSING_START = "DORMANT_PROCESSING_START: cutoffDate={}, candidateCount={}"
DORMANT_USER_PROCESSED = "DORMANT_USER_PROCESSED: userId={}, lastActiveAt={}"

// 알림 처리
NOTIFICATION_SCHEDULER_START = "NOTIFICATION_SCHEDULER_START: jobType={}, batchSize={}"
NOTIFICATION_CLEANUP_COMPLETE = "NOTIFICATION_CLEANUP_COMPLETE: expiredCount={}, oldCount={}, duration={}ms"
```

## 사용법

### 1. 일반적인 로깅

```java
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void createUser(String username) {
        log.info("Creating user: username={}", username);
        
        try {
            // 비즈니스 로직
            log.info("User created successfully: username={}", username);
        } catch (Exception e) {
            log.error("Failed to create user: username={}, error={}", username, e.getMessage(), e);
            throw e;
        }
    }
}
```

### 2. 스케줄러 로깅

```java
@Component  
public class NotificationScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    
    @Scheduled(cron = "0 0 2 * * *")
    public void processDormantUsers() {
        long startTime = System.currentTimeMillis();
        log.info(LogFormats.SCHEDULER_START, "processDormantUsers", LocalDateTime.now());

        try {
            int processedCount = userFacade.processDormantUsers();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info(LogFormats.SCHEDULER_COMPLETE, "processDormantUsers", duration, processedCount);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(LogFormats.SCHEDULER_ERROR, "processDormantUsers", e.getMessage(), duration, e);
        }
    }
}
```

## MDC (Mapped Diagnostic Context)

모든 HTTP 요청에 대해 자동으로 설정됩니다:

- `requestId`: 8자리 요청 고유 ID
- `userId`: 인증된 사용자 ID (인증 시스템 연동 후 활성화)
- `requestUri`: 요청 URI
- `requestMethod`: HTTP 메서드

### 로그 예시

**개발 환경**
```
2024-01-15 10:30:45.123 [http-nio-8080-exec-1] INFO  c.p.user.UserService [abc12345] [user123] - Creating user: username=testuser
```

**운영 환경 (JSON)**
```json
{
  "timestamp": "2024-01-15T10:30:45.123+09:00",
  "level": "INFO",
  "logger": "com.puppytalk.user.UserService",
  "message": "Creating user: username=testuser",
  "mdc": {
    "requestId": "abc12345",
    "userId": "user123",
    "requestUri": "/api/users",
    "requestMethod": "POST"
  },
  "app": "puppytalk-server",
  "env": "prod"
}
```

## 로그 레벨 설정

### 환경별 로그 레벨

| 패키지 | Local | Dev | Prod |
|--------|-------|-----|------|
| com.puppytalk | DEBUG | INFO | INFO |
| com.puppytalk.scheduler | DEBUG | DEBUG | INFO |
| com.puppytalk.user | DEBUG | DEBUG | INFO |
| com.puppytalk.auth | DEBUG | INFO | INFO |
| org.springframework | WARN | WARN | WARN |
| org.hibernate.SQL | DEBUG | INFO | WARN |

### 동적 로그 레벨 변경

애플리케이션 실행 중 로그 레벨을 변경하려면:

```bash
# 특정 패키지의 로그 레벨을 DEBUG로 변경
curl -X POST "http://localhost:8080/actuator/loggers/com.puppytalk.user" \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel": "DEBUG"}'
```

## 로그 파일 관리

### 운영 환경 로그 파일

- **메인 로그**: `logs/puppytalk.log`
- **에러 로그**: `logs/puppytalk-error.log`
- **로테이션**: 일별, 100MB 단위
- **보관 기간**: 메인 로그 30일, 에러 로그 90일
- **압축**: gzip

### 로그 모니터링

로그 분석 도구와 연동하기 위한 JSON 구조:

```json
{
  "timestamp": "ISO 8601 format",
  "level": "INFO|WARN|ERROR|DEBUG",
  "logger": "logger name",
  "message": "log message",
  "mdc": {
    "requestId": "request identifier",
    "userId": "user identifier",
    "requestUri": "request path",
    "requestMethod": "HTTP method"
  },
  "stackTrace": "exception stack trace if present",
  "app": "puppytalk-server",
  "env": "environment name"
}
```

## 모범 사례

### 1. 로그 메시지 작성
- 구조화된 정보 제공: `key=value` 형태 사용
- 민감한 정보 제외: 비밀번호, 토큰 등
- 컨텍스트 정보 포함: 사용자 ID, 요청 ID 등

### 2. 로그 레벨 선택
- **ERROR**: 시스템 장애, 처리 불가능한 예외
- **WARN**: 비정상적이지만 처리 가능한 상황
- **INFO**: 주요 비즈니스 이벤트, 스케줄러 실행 결과
- **DEBUG**: 개발 및 디버깅 정보

### 3. 성능 고려사항
- SLF4J 플레이스홀더 사용: `log.info("User: {}", username)`
- 로그 레벨 확인: `if (log.isDebugEnabled())`
- 비용이 큰 로그는 조건부 실행

## 문제 해결

### 로그가 출력되지 않을 때
1. 로그 레벨 확인
2. 로거 이름 확인
3. `logback-spring.xml` 설정 확인

### 운영 환경에서 로그 파일 확인
```bash
# 최근 로그 확인
tail -f logs/puppytalk.log

# 에러 로그만 확인  
tail -f logs/puppytalk-error.log

# 특정 키워드 검색
grep "SCHEDULER_ERROR" logs/puppytalk.log
```