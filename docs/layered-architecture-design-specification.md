# Layered Architecture Design Specification
## Puppy Talk Server - 계층형 아키텍처 설계 명세서

---

## 📋 목차
1. [아키텍처 개요](#아키텍처-개요)
2. [계층별 설계 명세](#계층별-설계-명세)
3. [인터페이스 설계](#인터페이스-설계)
4. [의존성 규칙](#의존성-규칙)
5. [아키텍처 품질 속성](#아키텍처-품질-속성)
6. [설계 패턴 및 원칙](#설계-패턴-및-원칙)
7. [확장성 설계](#확장성-설계)

---

## 🏗️ 아키텍처 개요

### 아키텍처 스타일
**Layered Architecture (계층형 아키텍처)** with **Hexagonal Architecture** principles

### 핵심 설계 원칙
1. **계층 분리 (Layer Separation)**: 명확한 책임 분담
2. **단방향 의존성 (Unidirectional Dependencies)**: 상위 → 하위 계층만 의존
3. **의존성 역전 (Dependency Inversion)**: 인터페이스 기반 추상화
4. **관심사 분리 (Separation of Concerns)**: 단일 책임 원칙

### 아키텍처 품질 목표
- **유지보수성 (Maintainability)**: 모듈 간 느슨한 결합
- **확장성 (Scalability)**: 새로운 기능 추가 용이성
- **테스트 용이성 (Testability)**: 계층별 독립적 테스트
- **가독성 (Readability)**: 직관적인 코드 구조

---

## 🎯 계층별 설계 명세

### Layer 1: Application Layer (애플리케이션 계층)
**위치**: 최상위 계층  
**모듈**: `application-api`

#### 책임
- Spring Boot 애플리케이션 부트스트랩
- 전체 시스템 구성 및 조립 (Composition Root)
- 외부 환경 설정 관리
- 애플리케이션 라이프사이클 관리

#### 핵심 구성요소
```java
@SpringBootApplication
public class PuppyTalkApplication {
    // 애플리케이션 진입점
}

@Configuration
public class WebSocketConfiguration {
    // WebSocket 설정
}

@Configuration  
public class WebConfiguration {
    // Web MVC 설정
}
```

#### 설계 특징
- 외부 의존성 없이 모든 하위 계층을 조립
- 환경별 프로필 관리 (local, docker, test)
- Cross-cutting concerns 설정

---

### Layer 2: Presentation Layer (프레젠테이션 계층)
**위치**: 상위 계층  
**모듈**: `api`

#### 책임
- HTTP 요청/응답 처리
- 데이터 변환 (DTO ↔ Domain Model)
- 입력 유효성 검증
- RESTful API 엔드포인트 제공
- WebSocket 연결 관리

#### 핵심 구성요소
```java
@RestController
@RequestMapping("/api/pets")
public class PetController {
    // REST API 엔드포인트
}

@Controller
public class ChatWebSocketController {
    // WebSocket 메시지 핸들링
}

// Request/Response DTOs
public record PetCreateRequest(String name, String breed, int age) {}
public record PetResponse(Long id, String name, String breed) {}
```

#### 설계 특징
- 컨트롤러는 비즈니스 로직을 포함하지 않음
- DTO를 통한 데이터 계약 정의
- HTTP 상태 코드 및 예외 처리

---

### Layer 3: Business Logic Layer (비즈니스 로직 계층)
**위치**: 중간 계층  
**모듈**: `service`, `ai-service`, `push-service`

#### 3.1 Core Service (`service`)
##### 책임
- 핵심 비즈니스 규칙 구현
- 트랜잭션 관리
- 도메인 객체 간 협업 조율
- 복잡한 비즈니스 플로우 관리

##### 핵심 구성요소
```java
@Service
@Transactional(readOnly = true)
public class ChatService {
    // 채팅 비즈니스 로직
}

@Service
@Transactional(readOnly = true) 
public class PetRegistrationService {
    // 반려동물 등록 비즈니스 로직
}

@Component
@Scheduled
public class InactivityNotificationScheduler {
    // 비활성 알림 스케줄링
}
```

#### 3.2 AI Integration Service (`ai-service`)
##### 책임
- 외부 AI 서비스 통합
- AI 제공업체 추상화
- Fallback 및 Circuit Breaker 패턴

##### 핵심 구성요소
```java
@Component
public class AiProviderFactory {
    // AI 제공업체 팩토리
}

public interface AiProvider {
    // AI 제공업체 추상화
}

@Component
public class AiResponseAdapter implements AiResponsePort {
    // Hexagonal Architecture 어댑터
}
```

#### 3.3 Push Notification Service (`push-service`)
##### 책임
- 외부 푸시 서비스 통합 (Firebase FCM)
- 푸시 알림 템플릿 관리
- 디바이스 토큰 관리

---

### Layer 4: Data Access Layer (데이터 접근 계층)
**위치**: 하위 계층  
**모듈**: `infrastructure`, `repository-jdbc`

#### 4.1 Infrastructure Interfaces (`infrastructure`)
##### 책임
- 데이터 접근 계약 정의
- 외부 서비스 포트 인터페이스
- Hexagonal Architecture의 포트 역할

##### 핵심 구성요소
```java
public interface PetRepository {
    Optional<Pet> findByIdentity(PetIdentity identity);
    List<Pet> findByUserId(UserIdentity userId);
    Pet save(Pet pet);
}

public interface AiResponsePort {
    String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory);
}

public interface PushNotificationSender {
    void sendPushNotification(PushNotification notification);
}
```

#### 4.2 JDBC Implementation (`repository-jdbc`)
##### 책임
- 실제 데이터베이스 조작
- SQL 쿼리 실행
- 트랜잭션 지원

##### 핵심 구성요소
```java
@Repository
public class PetJdbcRepository implements PetRepository {
    // JDBC 기반 Pet 데이터 접근
}

@Repository  
public class MessageJdbcRepository implements MessageRepository {
    // JDBC 기반 Message 데이터 접근
}
```

---

### Layer 5: Domain Layer (도메인 계층)
**위치**: 최하위 계층 (Core)  
**모듈**: `model`, `exception`

#### 5.1 Domain Model (`model`)
##### 책임
- 순수 도메인 엔티티 정의
- 비즈니스 규칙 캡슐화
- 도메인 불변식 보장

##### 핵심 구성요소
```java
public class Pet {
    private final PetIdentity identity;
    private final UserIdentity userId;
    private final PersonaIdentity personaId;
    private final String name;
    
    // 비즈니스 규칙: 1Pet = 1Persona (불변)
    public Pet(PetIdentity identity, UserIdentity userId, PersonaIdentity personaId, String name, String breed, int age, String imageUrl) {
        this.identity = Objects.requireNonNull(identity);
        this.userId = Objects.requireNonNull(userId);
        this.personaId = Objects.requireNonNull(personaId);
        this.name = validateName(name);
    }
}
```

#### 5.2 Domain Exceptions (`exception`)
##### 책임
- 비즈니스 예외 정의
- 도메인 규칙 위반 표현

---

### Layer 6: Infrastructure Layer (인프라스트럭처 계층)
**위치**: 독립적 계층  
**모듈**: `schema`

#### Schema Management (`schema`)
##### 책임
- 데이터베이스 스키마 관리 (Liquibase)
- 데이터 마이그레이션
- DDL 스크립트 버전 관리

---

## 🔌 인터페이스 설계

### 계층 간 인터페이스 원칙

#### 1. Port-Adapter Pattern (Hexagonal Architecture)
```java
// Port (Interface) - infrastructure 모듈
public interface AiResponsePort {
    String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory);
}

// Adapter (Implementation) - ai-service 모듈
@Component
public class AiResponseAdapter implements AiResponsePort {
    // 실제 AI 서비스 연동 구현
}
```

#### 2. Repository Pattern
```java
// Repository Interface - infrastructure 모듈
public interface MessageRepository {
    List<Message> findByChatRoomId(ChatRoomIdentity chatRoomId);
    Message save(Message message);
}

// Repository Implementation - repository-jdbc 모듈
@Repository
public class MessageJdbcRepository implements MessageRepository {
    // JDBC 기반 구현
}
```

#### 3. Service Interface (선택적)
```java
// 복잡한 비즈니스 로직의 경우 인터페이스 제공
public interface ChatService {
    ChatStartResult startChatWithPet(PetIdentity petId);
    MessageSendResult sendMessage(MessageSendCommand command);
}
```

### 데이터 전달 객체 (DTO) 설계

#### Request DTOs (API → Service)
```java
public record PetCreateRequest(
    @NotBlank String name,
    @NotBlank String breed, 
    @Min(0) int age,
    PersonaIdentity personaId
) {}
```

#### Response DTOs (Service → API)
```java
public record PetResponse(
    Long id,
    String name,
    String breed,
    int age,
    PersonaResponse persona
) {}
```

#### Command Objects (Service 내부)
```java
public record MessageSendCommand(
    ChatRoomIdentity chatRoomId,
    UserIdentity senderId,
    String content,
    LocalDateTime timestamp
) {}
```

#### Result Objects (Service 결과)
```java
public record MessageSendResult(
    Message userMessage,
    Message aiResponse,
    boolean aiResponseGenerated,
    boolean broadcastSent
) {}
```

---

## ⚡ 의존성 규칙

### 1. 계층 간 의존성 (Layer Dependencies)

#### 허용되는 의존성 (Top-down Only)
```
Application Layer
    ↓
Presentation Layer  
    ↓
Business Logic Layer
    ↓
Data Access Layer
    ↓
Domain Layer
```

#### 금지되는 의존성
```
❌ 하위 계층 → 상위 계층
❌ Domain Layer → 다른 모든 계층
❌ Data Access Layer → Business Logic Layer
❌ 순환 의존성 (Circular Dependencies)
```

### 2. 모듈 간 의존성 (Module Dependencies)

#### Gradle 의존성 타입
```gradle
dependencies {
    api project(':model')                    // 타입 노출 필요
    implementation project(':infrastructure') // 내부 구현에만 사용
    testImplementation project(':ai-service') // 테스트에만 사용
}
```

#### 의존성 방향 검증
```java
// ✅ 올바른 의존성
@Service
public class ChatService {
    private final PetRepository petRepository;        // Infrastructure interface
    private final AiResponsePort aiResponsePort;     // Infrastructure interface
    private final PersonaLookUpService personaService; // Same layer (문제 요소)
}

// ❌ 잘못된 의존성 (수정 필요)
@Repository
public class PetJdbcRepository {
    private final PetService petService; // 하위 계층이 상위 계층 의존
}
```

### 3. 의존성 주입 패턴

#### Constructor Injection (권장)
```java
@Service
@RequiredArgsConstructor
public class ChatService {
    private final MessageRepository messageRepository;
    private final AiResponsePort aiResponsePort;
    // 불변성과 테스트 용이성 보장
}
```

---

## 🎯 아키텍처 품질 속성

### 1. 유지보수성 (Maintainability)
- **모듈화**: 각 계층별 명확한 책임 분담
- **결합도**: 인터페이스 기반 느슨한 결합
- **응집도**: 관련 기능의 높은 응집도

### 2. 확장성 (Scalability)
- **수평 확장**: 계층별 독립적 스케일링 가능
- **기능 확장**: 새로운 모듈 추가 용이
- **마이크로서비스 전환**: 각 모듈의 독립 서비스 전환 가능

### 3. 테스트 용이성 (Testability)
- **단위 테스트**: Mock 기반 계층별 독립 테스트
- **통합 테스트**: 계층 간 상호작용 테스트
- **의존성 격리**: 인터페이스 기반 테스트 더블

### 4. 성능 (Performance)
- **트랜잭션 최적화**: 서비스 계층에서 트랜잭션 경계 관리
- **캐싱**: 계층별 적절한 캐싱 전략
- **지연 로딩**: 필요시점 데이터 로딩

---

## 🛠️ 설계 패턴 및 원칙

### 적용된 설계 패턴

#### 1. Hexagonal Architecture (Port-Adapter Pattern)
```java
// Port (Interface)
public interface AiResponsePort {
    String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory);
}

// Adapter (Implementation)
@Component
public class AiResponseAdapter implements AiResponsePort {
    // AI 서비스 연동 구현
}
```

#### 2. Repository Pattern
```java
public interface PetRepository {
    Optional<Pet> findByIdentity(PetIdentity identity);
}

@Repository
public class PetJdbcRepository implements PetRepository {
    // JDBC 구현
}
```

#### 3. Factory Pattern
```java
@Component
public class AiProviderFactory {
    public AiProvider getAvailableProvider() {
        // 사용 가능한 AI 제공업체 선택
    }
}
```

#### 4. Command Pattern  
```java
public record MessageSendCommand(
    ChatRoomIdentity chatRoomId,
    UserIdentity senderId, 
    String content
) {}
```

#### 5. Observer Pattern (WebSocket)
```java
@Component
public class WebSocketChatService {
    public void broadcastMessage(ChatMessage message) {
        // 실시간 메시지 브로드캐스트
    }
}
```

### SOLID 원칙 적용

#### Single Responsibility Principle
- 각 클래스와 모듈은 단일 책임
- 계층별 명확한 역할 분담

#### Open-Closed Principle
- 인터페이스 기반 확장 가능 설계
- 새로운 AI 제공업체 추가 시 기존 코드 수정 없음

#### Liskov Substitution Principle
- 구현체 교체 가능한 인터페이스 설계

#### Interface Segregation Principle
- 클라이언트별 필요한 인터페이스만 제공

#### Dependency Inversion Principle
- 상위 모듈이 하위 모듈의 구체적 구현에 의존하지 않음

---

## 🚀 확장성 설계

### 1. 새로운 기능 추가
```java
// 새로운 서비스 추가
@Service
public class GameService {
    private final PetRepository petRepository;
    private final GameRepository gameRepository;
    // 기존 infrastructure 인터페이스 재사용
}

// 새로운 Repository 인터페이스
public interface GameRepository {
    List<Game> findByPetId(PetIdentity petId);
}
```

### 2. 마이크로서비스 전환 준비
```yaml
# 각 모듈이 독립 서비스로 분리 가능
services:
  pet-service:
    - PetRegistrationService
    - PetLookUpService
    
  chat-service:
    - ChatService
    - MessageService
    
  notification-service:
    - InactivityNotificationService
    - PushNotificationService
```

### 3. 다양한 데이터베이스 지원
```java
// NoSQL Repository 구현 추가
@Repository
public class PetMongoRepository implements PetRepository {
    // MongoDB 기반 구현
}

// Redis Cache Repository
@Repository  
public class PetCacheRepository implements PetRepository {
    // Redis 기반 캐시 구현
}
```

### 4. 메시지 큐 통합
```java
// 비동기 메시징 포트 추가
public interface MessageQueuePort {
    void publishInactivityNotification(InactivityNotification notification);
}

// RabbitMQ 어댑터
@Component
public class RabbitMQAdapter implements MessageQueuePort {
    // 메시지 큐 연동
}
```

---

## 📋 아키텍처 검증 체크리스트

### ✅ 계층 분리 검증
- [ ] 각 계층의 책임이 명확하게 분리되어 있는가?
- [ ] 계층 간 인터페이스가 잘 정의되어 있는가?
- [ ] Cross-cutting concerns가 적절히 처리되고 있는가?

### ✅ 의존성 규칙 검증
- [ ] 모든 의존성이 단방향 (상위 → 하위)인가?
- [ ] 순환 의존성이 존재하지 않는가?
- [ ] Domain Layer가 외부 라이브러리에 의존하지 않는가?

### ✅ 인터페이스 설계 검증
- [ ] 모든 외부 시스템 연동이 인터페이스로 추상화되어 있는가?
- [ ] Repository 패턴이 올바르게 적용되어 있는가?
- [ ] DTO와 Domain Model이 적절히 분리되어 있는가?

### ✅ 테스트 용이성 검증
- [ ] 각 계층을 독립적으로 테스트할 수 있는가?
- [ ] Mock을 사용한 단위 테스트가 가능한가?
- [ ] 통합 테스트 시나리오가 명확한가?

---

## 🏁 결론

본 Layered Architecture 설계는 다음과 같은 목표를 달성합니다:

### 🎯 달성 목표
1. **명확한 책임 분리**: 각 계층별 단일 책임 원칙
2. **유연한 확장성**: 새로운 기능 추가 용이
3. **높은 테스트 용이성**: Mock 기반 독립적 테스트
4. **마이크로서비스 준비**: 모듈별 독립 서비스 전환 가능
5. **외부 의존성 격리**: 인터페이스 기반 추상화

### 🚀 향후 발전 방향
1. **이벤트 기반 아키텍처**: Domain Events 도입
2. **CQRS 패턴**: 읽기/쓰기 모델 분리  
3. **마이크로서비스**: 각 모듈의 독립 서비스화
4. **API Gateway**: 외부 API 통합 관리
5. **분산 추적**: 마이크로서비스 간 요청 추적

본 설계 명세서는 Puppy Talk 서버의 안정적이고 확장 가능한 아키텍처 기반을 제공하며, 지속적인 개발과 운영에 필요한 구조적 토대를 마련합니다.