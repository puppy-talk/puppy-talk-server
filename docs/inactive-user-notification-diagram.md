# 비활성 사용자 알림 기능 다이어그램

## 시퀀스 다이어그램

```mermaid
sequenceDiagram
    participant Scheduler as Spring Scheduler
    participant NotificationFacade as NotificationFacade<br/>(Application Layer)
    participant ActivityDomainService as ActivityDomainService<br/>(Activity BC)
    participant NotificationDomainService as NotificationDomainService<br/>(Notification BC)
    participant AiService as AiInactivityNotificationService<br/>(Application Layer)
    participant ChatDomainService as ChatDomainService<br/>(Chat BC)
    
    Scheduler->>NotificationFacade: findInactiveUsersForNotification()
    
    Note over NotificationFacade: Cross-BC 오케스트레이션
    NotificationFacade->>ActivityDomainService: findInactiveUsers(2시간)
    ActivityDomainService-->>NotificationFacade: List<UserId>
    
    NotificationFacade->>NotificationDomainService: filterEligibleUsersForNotification(users)
    Note over NotificationDomainService: 중복 알림 방지<br/>일일 발송 제한 확인
    NotificationDomainService-->>NotificationFacade: List<UserId>
    
    NotificationFacade-->>Scheduler: List<Long> (적격 사용자 ID)
    
    loop 각 비활성 사용자
        Scheduler->>AiService: createInactivityNotification(userId, petId)
        
        AiService->>ChatDomainService: findChatRoomByUserAndPet(userId, petId)
        ChatDomainService-->>AiService: Optional<ChatRoom>
        
        AiService->>ChatDomainService: findRecentChatHistory(chatRoomId, 10)
        ChatDomainService-->>AiService: List<Message>
        
        Note over AiService: AI를 통해<br/>개인화된 알림 메시지 생성
        AiService->>AiService: generatePersonalizedMessage(chatHistory, persona)
        
        AiService->>NotificationDomainService: createInactivityNotification(userId, petId, chatRoomId, title, content)
        Note over NotificationDomainService: 중복 방지<br/>일일 제한 확인<br/>5분 후 발송 스케줄링
        NotificationDomainService-->>AiService: NotificationId
        
        AiService-->>Scheduler: 알림 생성 완료
    end
```

## 아키텍처 다이어그램

```mermaid
graph TB
    subgraph "Scheduler Layer"
        A["Spring Scheduler<br/>@Scheduled 2시간마다"]
    end
    
    subgraph "Application Layer"
        B["NotificationFacade<br/>findInactiveUsersForNotification"]
        C["AiInactivityNotificationService<br/>createInactivityNotification"]
    end
    
    subgraph "Activity BC"
        D["ActivityDomainService<br/>findInactiveUsers"]
        E["UserActivityRepository<br/>findInactiveUserIds"]
    end
    
    subgraph "Notification BC"
        F["NotificationDomainService<br/>filterEligibleUsers<br/>createInactivityNotification"]
        G["NotificationRepository<br/>exists/count methods"]
    end
    
    subgraph "Chat BC"
        H["ChatDomainService<br/>findChatRoom<br/>findRecentHistory"]
        I["ChatRoomRepository<br/>MessageRepository"]
    end
    
    subgraph "External Service"
        J["AI Service<br/>메시지 생성"]
    end
    
    A --> B
    B --> D
    B --> F
    A --> C
    C --> H
    C --> F
    C --> J
    D --> E
    F --> G
    H --> I
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style C fill:#f3e5f5
    style D fill:#e8f5e8
    style F fill:#e8f5e8
    style H fill:#e8f5e8
    style J fill:#fff3e0
```

## 클래스 다이어그램

```mermaid
classDiagram
    class NotificationFacade {
        -NotificationDomainService notificationDomainService
        -ActivityDomainService activityDomainService
        +findInactiveUsersForNotification() List~Long~
    }
    
    class ActivityDomainService {
        -UserActivityRepository userActivityRepository
        +findInactiveUsers(int hours) List~UserId~
    }
    
    class NotificationDomainService {
        -NotificationRepository notificationRepository
        +filterEligibleUsersForNotification(List~UserId~) List~UserId~
        +createInactivityNotification(UserId, PetId, ChatRoomId, String, String) NotificationId
        -isDuplicateInactivityNotification(UserId) boolean
        -isDailyLimitExceeded(UserId) boolean
    }
    
    class AiInactivityNotificationService {
        -NotificationDomainService notificationDomainService
        -ChatDomainService chatDomainService
        -AiService aiService
        +createInactivityNotification(Long, Long) void
    }
    
    class ChatDomainService {
        -ChatRoomRepository chatRoomRepository
        -MessageRepository messageRepository
        +findChatRoomByUserAndPet(UserId, PetId) Optional~ChatRoom~
        +findRecentChatHistory(ChatRoomId, int) List~Message~
    }
    
    NotificationFacade --> ActivityDomainService
    NotificationFacade --> NotificationDomainService
    AiInactivityNotificationService --> NotificationDomainService
    AiInactivityNotificationService --> ChatDomainService
```

## 플로우 차트

```mermaid
flowchart TD
    A["Spring Scheduler<br/>2시간마다 실행"] --> B["NotificationFacade<br/>findInactiveUsers"]
    
    B --> C["ActivityDomainService<br/>findInactiveUsers"]
    C --> D{"2시간 이내<br/>활동 없는 사용자?"}
    D -->|Yes| E["비활성 사용자 목록 반환"]
    D -->|No| F["빈 목록 반환"]
    
    E --> G["NotificationDomainService<br/>filterEligibleUsers"]
    G --> H{"중복 알림<br/>존재?"}
    H -->|Yes| I["해당 사용자 제외"]
    H -->|No| J{"일일 발송<br/>제한 초과?"}
    
    J -->|Yes| I
    J -->|No| K["알림 가능 사용자 목록"]
    
    I --> K
    K --> L["적격 사용자 목록 반환"]
    
    L --> M{"적격 사용자<br/>존재?"}
    M -->|No| N["종료"]
    M -->|Yes| O["각 사용자별 알림 생성 시작"]
    
    O --> P["AiInactivityNotificationService<br/>createInactivityNotification"]
    P --> Q["ChatDomainService로 채팅방 조회"]
    Q --> R{"채팅방<br/>존재?"}
    R -->|No| S["알림 생성 실패"]
    R -->|Yes| T["최근 채팅 히스토리 조회"]
    
    T --> U["AI 서비스로 개인화된 메시지 생성"]
    U --> V["NotificationDomainService<br/>createInactivityNotification"]
    V --> W["알림 생성 및 5분 후 발송 스케줄링"]
    
    W --> X["다음 사용자 처리"]
    X --> Y{"모든 사용자<br/>처리 완료?"}
    Y -->|No| P
    Y -->|Yes| Z["알림 생성 프로세스 완료"]
    
    style A fill:#e1f5fe
    style B fill:#f3e5f5
    style G fill:#e8f5e8
    style P fill:#f3e5f5
    style V fill:#e8f5e8
    style U fill:#fff3e0
```

## 데이터 플로우

```mermaid
graph LR
    subgraph "Input Data"
        A[UserActivity 테이블<br/>마지막 활동 시간]
        B[Notification 테이블<br/>기존 알림 이력]
        C[ChatRoom & Message<br/>채팅 히스토리]
        D[Pet 테이블<br/>반려동물 페르소나]
    end
    
    subgraph "Processing"
        E[비활성 사용자<br/>필터링]
        F[알림 가능 사용자<br/>필터링]
        G[AI 메시지<br/>생성]
        H[알림 생성<br/>& 스케줄링]
    end
    
    subgraph "Output Data"
        I[Notification 테이블<br/>새로운 알림 레코드]
        J[스케줄러<br/>5분 후 발송 대기]
    end
    
    A --> E
    E --> F
    B --> F
    F --> G
    C --> G
    D --> G
    G --> H
    H --> I
    H --> J
    
    style E fill:#e8f5e8
    style F fill:#e8f5e8
    style G fill:#fff3e0
    style H fill:#e8f5e8
```

## 핵심 비즈니스 로직

### 1. 비활성 사용자 탐지 (Activity BC)
- **기준**: 마지막 활동 시간이 2시간 이전
- **대상 활동**: 채팅 메시지 전송, 로그인/로그아웃
- **SQL**: `SELECT user_id FROM user_activities WHERE created_at < NOW() - INTERVAL 2 HOUR`

### 2. 알림 대상자 필터링 (Notification BC)
- **중복 방지**: 이미 CREATED/QUEUED 상태의 비활성 알림이 있는 사용자 제외
- **일일 제한**: 하루 5개 이상 알림을 받은 사용자 제외
- **격리 원칙**: 각 BC는 자신의 데이터만 확인

### 3. 개인화된 메시지 생성 (Application Layer)
- **채팅 히스토리**: 최근 10개 메시지 분석
- **반려동물 페르소나**: 개성에 맞는 말투와 내용
- **AI 서비스**: 컨텍스트 기반 자연스러운 메시지 생성

### 4. 알림 스케줄링 (Notification BC)
- **지연 발송**: 생성 후 5분 뒤 실제 발송
- **상태 관리**: CREATED → QUEUED → SENT/FAILED
- **재시도 로직**: 발송 실패 시 자동 재시도
