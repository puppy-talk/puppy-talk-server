# WebSocket 기반 Puppy Talk 채팅 시스템 다이어그램

## 1. 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph "Client Side"
        UI[웹/모바일 UI]
        WS_CLIENT[WebSocket Client<br/>STOMP.js]
        PUSH_CLIENT[Push Notification<br/>FCM Client]
    end

    subgraph "Server Side"
        WEBSOCKET[WebSocket Handler<br/>STOMP Broker]
        CHAT_CONTROLLER[ChatWebSocketController]
        CHAT_SERVICE[ChatService]
        AI_SERVICE[AI Service<br/>Multi-Provider]
        PUSH_SERVICE[Push Service<br/>FCM]
        SCHEDULER[Inactivity Scheduler]
    end

    subgraph "Storage"
        DB[(Database<br/>Messages, Users, Pets)]
        REDIS[(Redis<br/>Session Management)]
    end

    UI ←→ WS_CLIENT
    UI ←→ PUSH_CLIENT
    WS_CLIENT ←→ WEBSOCKET
    WEBSOCKET ←→ CHAT_CONTROLLER
    CHAT_CONTROLLER ←→ CHAT_SERVICE
    CHAT_SERVICE ←→ AI_SERVICE
    CHAT_SERVICE ←→ PUSH_SERVICE
    CHAT_SERVICE ←→ DB
    SCHEDULER ←→ CHAT_SERVICE
    PUSH_SERVICE ←→ PUSH_CLIENT
```

## 2. WebSocket 연결 및 초기화 과정

```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Server
    participant DB as Database
    participant AI as AI Service

    Note over C,AI: 1. WebSocket 연결 수립
    C->>WS: HTTP Upgrade to WebSocket (/ws)
    WS->>C: 101 Switching Protocols
    WS->>C: WebSocket Connection Established

    Note over C,AI: 2. STOMP 연결 및 구독
    C->>WS: STOMP CONNECT
    WS->>C: STOMP CONNECTED

    C->>WS: SUBSCRIBE /topic/chat/{chatRoomId}
    C->>WS: SUBSCRIBE /topic/chat/{chatRoomId}/typing
    C->>WS: SUBSCRIBE /topic/chat/{chatRoomId}/read
    C->>WS: SUBSCRIBE /user/{userId}/queue/messages

    Note over C,AI: 3. 채팅방 및 펫 정보 로드
    WS->>DB: 채팅방 정보 조회
    DB->>WS: 채팅방 및 펫 데이터
    WS->>C: 시스템 메시지: "채팅방에 연결되었습니다"
```

## 3. 사용자 메시지 전송 과정

```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Server
    participant CTRL as ChatController
    participant SVC as ChatService
    participant AI as AI Service
    participant DB as Database

    Note over C,AI: 사용자가 "안녕 멍멍이!" 메시지 전송

    C->>WS: STOMP SEND /app/chat/1/send<br/>{userId: 1, content: "안녕 멍멍이!"}
    WS->>CTRL: sendMessage()
    
    Note over CTRL,AI: 1. 사용자 메시지 처리
    CTRL->>SVC: sendMessageToPet(roomId, content)
    SVC->>DB: INSERT 사용자 메시지
    SVC->>DB: INSERT 활동 기록 (MESSAGE_SENT)
    
    Note over CTRL,AI: 2. 사용자 메시지 브로드캐스트
    SVC->>WS: 실시간 메시지 브로드캐스트
    WS->>C: STOMP MESSAGE /topic/chat/1<br/>{senderType: "USER", content: "안녕 멍멍이!"}

    Note over CTRL,AI: 3. AI 응답 생성
    SVC->>AI: generateResponse(persona, message)
    AI->>SVC: "멍! 주인이다! 오늘 뭐 했어? 🐕"
    
    Note over CTRL,AI: 4. AI 응답 저장 및 브로드캐스트
    SVC->>DB: INSERT AI 펫 메시지
    SVC->>WS: AI 응답 브로드캐스트
    WS->>C: STOMP MESSAGE /topic/chat/1<br/>{senderType: "PET", content: "멍! 주인이다! 오늘 뭐 했어? 🐕"}
```

## 4. 타이핑 상태 실시간 전송

```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Server
    participant OTHERS as Other Clients

    Note over C,OTHERS: 타이핑 상태 실시간 전송

    C->>WS: STOMP SEND /app/chat/1/typing<br/>{userId: 1, isTyping: true}
    WS->>OTHERS: STOMP MESSAGE /topic/chat/1/typing<br/>{messageType: "TYPING", userId: 1}
    
    Note over C,OTHERS: 3초 후 타이핑 중단
    C->>WS: STOMP SEND /app/chat/1/typing<br/>{userId: 1, isTyping: false}
    WS->>OTHERS: STOMP MESSAGE /topic/chat/1/typing<br/>{messageType: "STOP_TYPING", userId: 1}
```

## 5. AI 먼저 메시지 보내기 (핵심 기능)

```mermaid
sequenceDiagram
    participant SCHEDULER as Inactivity Scheduler
    participant DB as Database
    participant SVC as ChatService
    participant AI as AI Service
    participant WS as WebSocket Server
    participant PUSH as Push Service
    participant C as Client

    Note over SCHEDULER,C: 2시간 비활성 후 AI가 먼저 메시지 전송

    SCHEDULER->>DB: 2시간 이상 비활성 채팅방 조회
    DB->>SCHEDULER: 비활성 채팅방 목록

    loop 각 비활성 채팅방에 대해
        SCHEDULER->>SVC: sendInactivityMessage(chatRoomId)
        SVC->>AI: generateInactivityMessage(persona)
        AI->>SVC: "주인 어디 있어? 심심해! 멍!"
        
        SVC->>DB: INSERT AI 펫 메시지
        SVC->>DB: UPDATE 비활성 알림 상태
        
        alt 사용자가 WebSocket 연결 중인 경우
            SVC->>WS: 실시간 메시지 브로드캐스트
            WS->>C: STOMP MESSAGE /topic/chat/1<br/>{senderType: "PET", content: "주인 어디 있어? 심심해! 멍!"}
        else 사용자가 오프라인인 경우
            SVC->>PUSH: FCM 푸시 알림 전송
            PUSH->>C: Push Notification<br/>"🐶 멍멍이: 주인 어디 있어? 심심해! 멍!"
        end
    end
```

## 6. 읽음 처리 과정

```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Server
    participant SVC as ChatService
    participant DB as Database

    Note over C,DB: 메시지 읽음 처리

    C->>WS: STOMP SEND /app/chat/1/read<br/>{userId: 1, lastReadMessageId: 123}
    WS->>SVC: markMessagesAsRead(chatRoomId)
    
    SVC->>DB: UPDATE messages SET is_read = true
    SVC->>DB: INSERT 활동 기록 (MESSAGE_READ)
    
    SVC->>WS: 읽음 상태 브로드캐스트
    WS->>C: STOMP MESSAGE /topic/chat/1/read<br/>{messageType: "READ_RECEIPT", userId: 1}
```

## 7. 연결 상태 관리

```mermaid
stateDiagram-v2
    [*] --> Disconnected: 초기 상태
    
    Disconnected --> Connecting: WebSocket 연결 시도
    Connecting --> Connected: 연결 성공
    Connecting --> Disconnected: 연결 실패 (재시도)
    
    Connected --> Subscribed: STOMP 구독 완료
    Subscribed --> Chatting: 메시지 송수신 가능
    
    Chatting --> Chatting: 메시지 전송/수신
    Chatting --> Typing: 타이핑 상태
    Typing --> Chatting: 타이핑 완료
    
    Chatting --> Inactive: 2시간 비활성
    Inactive --> Chatting: 활동 재개
    Inactive --> PetInitiated: AI가 먼저 메시지
    PetInitiated --> Chatting: 사용자 응답
    
    Subscribed --> Disconnected: 연결 종료
    Chatting --> Disconnected: 연결 끊김
    Inactive --> Disconnected: 연결 끊김
    
    Disconnected --> [*]: 세션 종료
```

## 8. 에러 처리 및 재연결

```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Server
    participant SVC as ChatService

    Note over C,SVC: 네트워크 오류 발생

    C->>WS: 메시지 전송 시도
    WS--xC: 연결 끊김 (네트워크 오류)
    
    Note over C,SVC: 클라이언트 자동 재연결

    C->>WS: WebSocket 재연결 시도
    WS->>C: 연결 복구 성공
    
    C->>WS: STOMP 재연결 및 구독
    WS->>SVC: 미수신 메시지 조회
    SVC->>C: 누락된 메시지 일괄 전송
    
    C->>WS: 정상 메시지 송수신 재개
```

## 핵심 WebSocket 채널 구조

```
📡 WebSocket Endpoints:
├── /ws                              # WebSocket 연결 엔드포인트
│
📤 Send Destinations (클라이언트 → 서버):
├── /app/chat/{chatRoomId}/send      # 메시지 전송
├── /app/chat/{chatRoomId}/typing    # 타이핑 상태
└── /app/chat/{chatRoomId}/read      # 읽음 처리

📥 Subscribe Destinations (서버 → 클라이언트):
├── /topic/chat/{chatRoomId}         # 채팅 메시지 수신
├── /topic/chat/{chatRoomId}/typing  # 타이핑 상태 수신
├── /topic/chat/{chatRoomId}/read    # 읽음 상태 수신
├── /topic/chat/{chatRoomId}/system  # 시스템 메시지
└── /user/{userId}/queue/messages    # 개인 메시지 (AI 먼저 보내기)
```

## 주요 메시지 타입

```json
// 1. 일반 채팅 메시지
{
  "messageId": {"id": 123},
  "chatRoomId": {"id": 1},
  "userId": {"id": 1},
  "senderType": "USER" | "PET" | "SYSTEM",
  "content": "메시지 내용",
  "isRead": true,
  "timestamp": "2025-01-13T14:30:00",
  "messageType": "MESSAGE"
}

// 2. 타이핑 상태
{
  "chatRoomId": {"id": 1},
  "userId": {"id": 1},
  "senderType": "USER",
  "messageType": "TYPING" | "STOP_TYPING",
  "timestamp": "2025-01-13T14:30:00"
}

// 3. 읽음 확인
{
  "chatRoomId": {"id": 1},
  "userId": {"id": 1},
  "messageType": "READ_RECEIPT",
  "lastReadMessageId": 123,
  "timestamp": "2025-01-13T14:30:00"
}
```

이 다이어그램들이 Puppy Talk의 WebSocket 기반 실시간 채팅 시스템의 전체적인 흐름을 보여줍니다. 특히 "AI가 먼저 메시지 보내기" 기능이 WebSocket의 핵심적인 활용 사례임을 알 수 있습니다.