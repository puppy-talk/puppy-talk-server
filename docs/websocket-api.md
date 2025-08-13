# WebSocket 실시간 채팅 API

## 개요

Puppy Talk의 실시간 채팅 기능은 WebSocket과 STOMP 프로토콜을 사용하여 구현되었습니다. 사용자와 AI 펫 간의 실시간 메시지 교환, 타이핑 상태 표시, 읽음 처리를 지원합니다.

## 연결 설정

### WebSocket 엔드포인트
```
ws://localhost:8080/ws
```

### 클라이언트 라이브러리 (JavaScript 예시)
```javascript
// SockJS와 STOMP 클라이언트 사용
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    
    // 채팅방 구독
    subscribeToChatRoom(chatRoomId);
});
```

## 구독 (Subscribe) 채널

### 1. 채팅 메시지 수신
```javascript
stompClient.subscribe('/topic/chat/{chatRoomId}', function (message) {
    const chatMessage = JSON.parse(message.body);
    console.log('Received message:', chatMessage);
});
```

**메시지 형식:**
```json
{
    "messageId": { "id": 123 },
    "chatRoomId": { "id": 1 },
    "userId": { "id": 1 },
    "senderType": "USER" | "PET" | "SYSTEM",
    "content": "메시지 내용",
    "isRead": true,
    "timestamp": "2025-01-13T14:30:00",
    "messageType": "MESSAGE"
}
```

### 2. 타이핑 상태 수신
```javascript
stompClient.subscribe('/topic/chat/{chatRoomId}/typing', function (message) {
    const typingMessage = JSON.parse(message.body);
    console.log('Typing status:', typingMessage);
});
```

**타이핑 메시지 형식:**
```json
{
    "chatRoomId": { "id": 1 },
    "userId": { "id": 1 },
    "senderType": "USER",
    "messageType": "TYPING" | "STOP_TYPING",
    "timestamp": "2025-01-13T14:30:00"
}
```

### 3. 읽음 상태 수신
```javascript
stompClient.subscribe('/topic/chat/{chatRoomId}/read', function (message) {
    const readReceipt = JSON.parse(message.body);
    console.log('Read receipt:', readReceipt);
});
```

### 4. 시스템 메시지 수신
```javascript
stompClient.subscribe('/topic/chat/{chatRoomId}/system', function (message) {
    const systemMessage = JSON.parse(message.body);
    console.log('System message:', systemMessage);
});
```

### 5. 개인 메시지 수신
```javascript
stompClient.subscribe('/user/{userId}/queue/messages', function (message) {
    const privateMessage = JSON.parse(message.body);
    console.log('Private message:', privateMessage);
});
```

## 메시지 전송 (Send)

### 1. 채팅 메시지 전송
```javascript
const messageRequest = {
    userId: 1,
    content: "안녕하세요!"
};

stompClient.send('/app/chat/{chatRoomId}/send', {}, JSON.stringify(messageRequest));
```

### 2. 타이핑 상태 전송
```javascript
const typingRequest = {
    userId: 1,
    isTyping: true  // 또는 false
};

stompClient.send('/app/chat/{chatRoomId}/typing', {}, JSON.stringify(typingRequest));
```

### 3. 읽음 처리
```javascript
const readRequest = {
    userId: 1,
    lastReadMessageId: 123
};

stompClient.send('/app/chat/{chatRoomId}/read', {}, JSON.stringify(readRequest));
```

## 완전한 클라이언트 예시

```javascript
class PuppyTalkWebSocket {
    constructor(chatRoomId, userId) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.stompClient = null;
    }
    
    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Connected:', frame);
            this.subscribeToChannels();
        });
    }
    
    subscribeToChannels() {
        // 채팅 메시지 구독
        this.stompClient.subscribe(`/topic/chat/${this.chatRoomId}`, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.onMessageReceived(chatMessage);
        });
        
        // 타이핑 상태 구독
        this.stompClient.subscribe(`/topic/chat/${this.chatRoomId}/typing`, (message) => {
            const typingMessage = JSON.parse(message.body);
            this.onTypingStatusChanged(typingMessage);
        });
        
        // 읽음 상태 구독
        this.stompClient.subscribe(`/topic/chat/${this.chatRoomId}/read`, (message) => {
            const readReceipt = JSON.parse(message.body);
            this.onReadReceiptReceived(readReceipt);
        });
        
        // 개인 메시지 구독
        this.stompClient.subscribe(`/user/${this.userId}/queue/messages`, (message) => {
            const privateMessage = JSON.parse(message.body);
            this.onPrivateMessageReceived(privateMessage);
        });
    }
    
    sendMessage(content) {
        const messageRequest = {
            userId: this.userId,
            content: content
        };
        
        this.stompClient.send(`/app/chat/${this.chatRoomId}/send`, {}, 
                             JSON.stringify(messageRequest));
    }
    
    sendTypingStatus(isTyping) {
        const typingRequest = {
            userId: this.userId,
            isTyping: isTyping
        };
        
        this.stompClient.send(`/app/chat/${this.chatRoomId}/typing`, {}, 
                             JSON.stringify(typingRequest));
    }
    
    markAsRead(lastReadMessageId) {
        const readRequest = {
            userId: this.userId,
            lastReadMessageId: lastReadMessageId
        };
        
        this.stompClient.send(`/app/chat/${this.chatRoomId}/read`, {}, 
                             JSON.stringify(readRequest));
    }
    
    // 이벤트 핸들러들
    onMessageReceived(message) {
        console.log('New message:', message);
        // UI 업데이트 로직
    }
    
    onTypingStatusChanged(typingMessage) {
        console.log('Typing status:', typingMessage);
        // 타이핑 인디케이터 표시/숨김
    }
    
    onReadReceiptReceived(readReceipt) {
        console.log('Read receipt:', readReceipt);
        // 읽음 표시 업데이트
    }
    
    onPrivateMessageReceived(privateMessage) {
        console.log('Private message:', privateMessage);
        // 개인 알림 처리
    }
    
    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// 사용 예시
const webSocket = new PuppyTalkWebSocket(1, 1);
webSocket.connect();

// 메시지 전송
webSocket.sendMessage("안녕 멍멍이!");

// 타이핑 시작
webSocket.sendTypingStatus(true);

// 타이핑 중단
setTimeout(() => {
    webSocket.sendTypingStatus(false);
}, 3000);
```

## 메시지 타입

### ChatMessageType
- `MESSAGE`: 일반 채팅 메시지
- `TYPING`: 타이핑 시작
- `STOP_TYPING`: 타이핑 중단
- `READ_RECEIPT`: 읽음 확인
- `USER_JOINED`: 사용자 입장
- `USER_LEFT`: 사용자 퇴장
- `SYSTEM`: 시스템 메시지

### SenderType
- `USER`: 사용자가 보낸 메시지
- `PET`: AI 펫이 보낸 메시지
- `SYSTEM`: 시스템 메시지

## 푸시 알림과의 연동

WebSocket 연결이 끊어진 상태에서도 사용자에게 알림을 보내기 위해 푸시 알림 시스템과 연동됩니다:

1. **실시간 연결 시**: WebSocket을 통해 즉시 메시지 전달
2. **연결 해제 시**: FCM 푸시 알림으로 메시지 알림
3. **비활성 상태**: 2시간 후 AI 펫이 먼저 메시지를 보내며, WebSocket과 푸시 알림 모두 활용

## 보안 고려사항

현재 개발 단계에서는 기본적인 연결만 허용하지만, 프로덕션 환경에서는 다음 보안 기능들이 추가될 예정입니다:

- JWT 토큰 기반 인증
- 채팅방 접근 권한 검증
- Rate limiting
- 메시지 내용 검증

## 에러 처리

WebSocket 연결 오류나 메시지 전송 실패 시, 클라이언트는 적절한 재연결 로직을 구현해야 합니다:

```javascript
stompClient.connect({}, successCallback, function(error) {
    console.log('WebSocket connection failed:', error);
    // 재연결 로직 구현
    setTimeout(() => {
        webSocket.connect();
    }, 5000);
});
```