# 모듈 인터페이스 및 경계 정의

## 📋 개요

이 문서는 분리된 service 모듈들의 명확한 인터페이스와 경계를 정의하여 **Bounded Context** 원칙을 준수하고 모듈 간 결합도를 최소화합니다.

## 🔐 auth-service

### 책임 범위 (Bounded Context)
- **인증**: 로그인, 로그아웃, 토큰 발급
- **인가**: JWT 토큰 검증, 권한 확인
- **보안**: 비밀번호 해싱, 토큰 보안

### 공개 인터페이스
```java
// 인증 관련 서비스
public interface AuthService {
    AuthResult login(String username, String password);
    AuthResult register(RegisterRequest request);
    boolean validateToken(String token);
    UserIdentity getCurrentUser(String token);
}

// JWT 토큰 관리
public interface JwtTokenProvider {
    String generateToken(UserIdentity userIdentity);
    boolean validateToken(String token);
    UserIdentity parseToken(String token);
}

// 비밀번호 처리
public interface PasswordEncoder {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
```

### 입출력 DTO
```java
public record AuthResult(
    boolean success,
    String token,
    UserIdentity userIdentity,
    String errorMessage
) {}

public record RegisterRequest(
    String username,
    String email,
    String password
) {}
```

### 의존성 경계
- **허용**: `model`, `exception`, `infrastructure`, `repository-jdbc`
- **금지**: 다른 비즈니스 서비스 (`user-service`, `pet-service`, etc.)

## 👤 user-service

### 책임 범위 (Bounded Context)
- **사용자 관리**: 사용자 정보 CRUD
- **사용자 조회**: ID, username, email 기반 조회
- **프로필 관리**: 사용자 프로필 정보 관리

### 공개 인터페이스
```java
public interface UserLookUpService {
    Optional<User> findById(UserIdentity userIdentity);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
}

public interface UserManagementService {
    User updateProfile(UserIdentity userIdentity, UserUpdateRequest request);
    void deleteUser(UserIdentity userIdentity);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

### 입출력 DTO
```java
public record UserUpdateRequest(
    String email,
    String profileImageUrl
) {}

public record UserSummary(
    UserIdentity userIdentity,
    String username,
    String email
) {}
```

### 의존성 경계
- **허용**: `model`, `exception`, `infrastructure`, `repository-jdbc`
- **금지**: 다른 비즈니스 서비스

## 🐕 pet-service

### 책임 범위 (Bounded Context)
- **반려동물 관리**: 반려동물 등록, 수정, 삭제
- **페르소나 관리**: AI 페르소나 조회 및 관리
- **비즈니스 규칙**: 1Pet = 1Persona 규칙 적용

### 공개 인터페이스
```java
public interface PetRegistrationService {
    PetRegistrationResult registerPet(PetCreateCommand command);
    void deletePet(PetIdentity petIdentity, UserIdentity userIdentity);
}

public interface PetLookUpService {
    Optional<Pet> findById(PetIdentity petIdentity);
    List<Pet> findByOwner(UserIdentity userIdentity);
    boolean existsById(PetIdentity petIdentity);
    boolean isOwner(PetIdentity petIdentity, UserIdentity userIdentity);
}

public interface PersonaLookUpService {
    Optional<Persona> findById(PersonaIdentity personaIdentity);
    List<Persona> findActivePersonas();
    Optional<Persona> findByPet(PetIdentity petIdentity);
}
```

### 입출력 DTO
```java
public record PetCreateCommand(
    UserIdentity userIdentity,
    PersonaIdentity personaIdentity,
    String name,
    String breed,
    int age,
    String profileImageUrl
) {}

public record PetSummary(
    PetIdentity petIdentity,
    String name,
    String breed,
    PersonaSummary persona
) {}
```

### 의존성 경계
- **허용**: `model`, `exception`, `infrastructure`, `repository-jdbc`
- **금지**: 다른 비즈니스 서비스

## 💬 chat-service

### 책임 범위 (Bounded Context)
- **채팅방 관리**: 채팅방 생성, 조회, 관리
- **메시지 처리**: 메시지 송수신, 저장, 조회
- **실시간 통신**: WebSocket 채팅 처리
- **활동 추적**: 사용자 활동 기록 및 추적
- **디바이스 관리**: 푸시 알림용 디바이스 토큰 관리

### 공개 인터페이스
```java
public interface ChatService {
    ChatStartResult startChat(UserIdentity userIdentity, PetIdentity petIdentity);
    MessageSendResult sendMessage(MessageSendCommand command);
    List<Message> getMessages(ChatRoomIdentity chatRoomIdentity, UserIdentity userIdentity);
}

public interface ChatRoomLookUpService {
    Optional<ChatRoom> findById(ChatRoomIdentity chatRoomIdentity);
    Optional<ChatRoom> findByPet(PetIdentity petIdentity);
    boolean hasAccess(ChatRoomIdentity chatRoomIdentity, UserIdentity userIdentity);
}

public interface ActivityTrackingService {
    void trackActivity(UserIdentity userIdentity, ChatRoomIdentity chatRoomIdentity, ActivityType activityType);
    Optional<UserActivity> getLastActivity(UserIdentity userIdentity, ChatRoomIdentity chatRoomIdentity);
}

public interface WebSocketChatService {
    void handleMessage(ChatMessage chatMessage);
    void notifyMessage(ChatRoomIdentity chatRoomIdentity, Message message);
}
```

### 입출력 DTO
```java
public record MessageSendCommand(
    UserIdentity userIdentity,
    ChatRoomIdentity chatRoomIdentity,
    String content,
    SenderType senderType
) {}

public record ChatStartResult(
    ChatRoomIdentity chatRoomIdentity,
    String roomName,
    PetSummary pet
) {}
```

### 의존성 경계
- **허용**: `model`, `exception`, `infrastructure`, `repository-jdbc`, `user-service`, `pet-service`, `shared-dto`
- **특별**: `user-service`와 `pet-service`의 조회 인터페이스만 사용 (CRUD 아님)

### 외부 서비스 호출 패턴
```java
@Service
public class ChatServiceImpl implements ChatService {
    private final UserLookUpService userLookUpService;
    private final PetLookUpService petLookUpService;
    
    @Override
    public ChatStartResult startChat(UserIdentity userIdentity, PetIdentity petIdentity) {
        // 사용자 검증
        User user = userLookUpService.findById(userIdentity)
            .orElseThrow(() -> new UserNotFoundException(userIdentity));
            
        // 반려동물 검증 및 소유권 확인
        Pet pet = petLookUpService.findById(petIdentity)
            .orElseThrow(() -> new PetNotFoundException(petIdentity));
            
        if (!petLookUpService.isOwner(petIdentity, userIdentity)) {
            throw new UnauthorizedAccessException();
        }
        
        // 채팅방 로직 실행...
    }
}
```

## 📢 notification-service

### 책임 범위 (Bounded Context)
- **비활성 알림**: 2시간 비활성 사용자 알림 처리
- **스케줄링**: 알림 스케줄링 및 실행
- **푸시 알림**: 푸시 알림 발송 관리
- **실시간 알림**: WebSocket을 통한 실시간 알림

### 공개 인터페이스
```java
public interface InactivityNotificationService {
    void processInactivityNotifications();
    void createOrUpdateNotification(ChatRoomIdentity chatRoomIdentity, LocalDateTime lastActivity);
    void disableNotification(ChatRoomIdentity chatRoomIdentity);
}

public interface PushNotificationService {
    void sendPushNotification(PushNotificationRequest request);
    void scheduleNotification(NotificationScheduleRequest request);
}

public interface RealtimeNotificationPort {
    void sendRealtimeNotification(ChatRoomIdentity chatRoomIdentity, String message);
}
```

### 입출력 DTO
```java
public record PushNotificationRequest(
    UserIdentity userIdentity,
    String title,
    String body,
    Map<String, String> data
) {}

public record NotificationScheduleRequest(
    ChatRoomIdentity chatRoomIdentity,
    LocalDateTime scheduledTime,
    String message
) {}
```

### 의존성 경계
- **허용**: `model`, `exception`, `infrastructure`, `repository-jdbc`, `chat-service`, `shared-dto`
- **제한**: `chat-service`의 읽기 전용 메서드만 사용

## 📦 shared-dto

### 책임 범위 (Bounded Context)
- **공통 DTO**: 모듈 간 데이터 전달 객체
- **커맨드 객체**: 복합 작업을 위한 커맨드 패턴 구현
- **결과 객체**: 작업 결과를 담는 객체

### 공개 인터페이스
```java
// 채팅 관련 공통 DTO
public record ChatStartResult(
    ChatRoomIdentity chatRoomIdentity,
    String roomName,
    PetSummary pet
) {}

public record MessageSendResult(
    MessageIdentity messageIdentity,
    String content,
    LocalDateTime createdAt,
    boolean success
) {}

// 반려동물 관련 공통 DTO
public record PetRegistrationResult(
    PetIdentity petIdentity,
    String name,
    boolean success,
    String errorMessage
) {}

// 커맨드 객체들
public record MessageSendCommand(
    UserIdentity userIdentity,
    ChatRoomIdentity chatRoomIdentity,
    String content,
    SenderType senderType
) {}

public record PetCreateCommand(
    UserIdentity userIdentity,
    PersonaIdentity personaIdentity,
    String name,
    String breed,
    int age,
    String profileImageUrl
) {}
```

### 의존성 경계
- **허용**: `model`만
- **금지**: 모든 다른 모듈 (순수 데이터 객체)

## 🛡️ 모듈 경계 보호 메커니즘

### 1. 인터페이스 기반 설계
- 각 모듈은 명확한 public 인터페이스만 노출
- 구현체는 package-private으로 캡슐화

### 2. 의존성 방향 제한
```java
// ✅ 허용되는 패턴
@Service
public class ChatServiceImpl {
    private final UserLookUpService userLookUpService; // 조회만
    private final PetLookUpService petLookUpService;   // 조회만
}

// ❌ 금지되는 패턴
@Service 
public class UserServiceImpl {
    private final ChatService chatService; // 역방향 의존성
}
```

### 3. 이벤트 기반 통신 (미래 확장)
```java
// 모듈 간 직접 호출 대신 이벤트 발행
@EventListener
public class NotificationEventHandler {
    public void handleUserActivity(UserActivityEvent event) {
        // 비동기 알림 처리
    }
}
```

### 4. 컴파일 타임 경계 검사
- Gradle의 모듈 의존성을 통해 컴파일 타임에 의존성 위반 차단
- ArchUnit 등의 도구로 아키텍처 규칙 자동 검증 (선택사항)

## 🔄 모듈 간 협력 패턴

### 패턴 1: 조회 전용 협력
```java
// chat-service에서 user-service 사용
User user = userLookUpService.findById(userIdentity)
    .orElseThrow(() -> new UserNotFoundException(userIdentity));
```

### 패턴 2: 커맨드 전달
```java
// API에서 복수 서비스 조합
@PostMapping("/pets")
public ResponseEntity<PetCreateResponse> createPet(@RequestBody PetCreateRequest request) {
    PetCreateCommand command = mapToCommand(request);
    PetRegistrationResult result = petRegistrationService.registerPet(command);
    return ResponseEntity.ok(mapToResponse(result));
}
```

### 패턴 3: 이벤트 기반 (향후 구현)
```java
// 직접 호출 대신 이벤트 발행
applicationEventPublisher.publishEvent(new UserActivityEvent(userIdentity, activityType));
```

이러한 명확한 인터페이스와 경계 정의를 통해 각 모듈의 **단일 책임 원칙**을 보장하고, **높은 응집도**와 **낮은 결합도**를 달성할 수 있습니다.