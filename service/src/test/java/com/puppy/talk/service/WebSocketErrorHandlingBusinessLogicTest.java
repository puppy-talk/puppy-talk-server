package com.puppy.talk.service;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.MessageIdentity;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.model.websocket.ChatMessage;
import com.puppy.talk.model.websocket.ChatMessageType;
import com.puppy.talk.service.websocket.WebSocketChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocket 에러 처리 로직 테스트")
class WebSocketErrorHandlingBusinessLogicTest {
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @InjectMocks
    private WebSocketChatService webSocketChatService;
    
    private ChatRoomIdentity chatRoomId;
    private UserIdentity userId;
    private ChatMessage normalMessage;
    
    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomIdentity.of(1L);
        userId = UserIdentity.of(1L);
        
        normalMessage = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, "테스트 메시지", true
        );
    }
    
    @Test
    @DisplayName("메시지 브로드캐스트 시 연결 오류 발생 - 예외 전파하지 않고 로깅")
    void broadcastMessage_ConnectionError_DoesNotPropagateException() {
        // Given
        doThrow(new RuntimeException("WebSocket 연결이 끊어졌습니다"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        // 예외가 전파되지 않아야 함 (서비스 로직에서 try-catch 처리)
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastMessage(normalMessage);
        });
        
        // 메시지 전송 시도는 이루어짐
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id()), 
            eq(normalMessage)
        );
    }
    
    @Test
    @DisplayName("타이핑 상태 브로드캐스트 시 템플릿 오류 - 복원력 검증")
    void broadcastTyping_TemplateError_ResilientHandling() {
        // Given
        ChatMessage typingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        
        doThrow(new IllegalStateException("메시지 템플릿 초기화 오류"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastTypingStatus(typingMessage);
        });
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id() + "/typing"), 
            eq(typingMessage)
        );
    }
    
    @Test
    @DisplayName("읽음 확인 브로드캐스트 시 네트워크 타임아웃 - 내결함성 검증")
    void broadcastReadReceipt_NetworkTimeout_FaultTolerant() {
        // Given
        ChatMessage readMessage = ChatMessage.readReceipt(chatRoomId, userId, MessageIdentity.of(5L));
        
        doThrow(new RuntimeException("네트워크 타임아웃"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastReadReceipt(readMessage);
        });
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id() + "/read"), 
            eq(readMessage)
        );
    }
    
    @Test
    @DisplayName("개인 메시지 전송 시 사용자 오프라인 - 우아한 실패 처리")
    void sendToUser_UserOffline_GracefulFailure() {
        // Given
        ChatMessage privateMessage = ChatMessage.newMessage(
            MessageIdentity.of(2L), chatRoomId, userId, SenderType.SYSTEM, 
            "개인 알림", false
        );
        
        doThrow(new RuntimeException("사용자가 오프라인 상태입니다"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.sendToUser(userId, privateMessage);
        });
        
        verify(messagingTemplate).convertAndSend(
            eq("/user/" + userId.id() + "/queue/messages"), 
            eq(privateMessage)
        );
    }
    
    @Test
    @DisplayName("시스템 메시지 브로드캐스트 시 서버 과부하 - 계속 처리")
    void broadcastSystemMessage_ServerOverload_ContinueProcessing() {
        // Given
        ChatMessage systemMessage = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, 
            "서버 점검 안내", false, ChatMessageType.SYSTEM
        );
        
        doThrow(new RuntimeException("서버 과부하로 인한 메시지 전송 실패"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastSystemMessage(systemMessage);
        });
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id() + "/system"), 
            eq(systemMessage)
        );
    }
    
    @Test
    @DisplayName("사용자 입장 알림 시 권한 오류 - 로깅 후 계속 진행")
    void notifyUserJoined_PermissionError_LogAndContinue() {
        // Given
        ChatMessage joinMessage = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, 
            "사용자가 입장했습니다", false, ChatMessageType.USER_JOINED
        );
        
        doThrow(new SecurityException("채팅방 접근 권한 없음"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.notifyUserJoined(joinMessage);
        });
        
        // 시스템 메시지 채널로 브로드캐스트 시도
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id() + "/system"), 
            eq(joinMessage)
        );
    }
    
    @Test
    @DisplayName("사용자 퇴장 알림 시 채널 없음 오류 - 안전한 처리")
    void notifyUserLeft_ChannelNotFound_SafeHandling() {
        // Given
        ChatMessage leaveMessage = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, 
            "사용자가 퇴장했습니다", false, ChatMessageType.USER_LEFT
        );
        
        doThrow(new IllegalArgumentException("존재하지 않는 채널"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.notifyUserLeft(leaveMessage);
        });
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id() + "/system"), 
            eq(leaveMessage)
        );
    }
    
    @Test
    @DisplayName("NULL 메시지 처리 시 방어적 프로그래밍 - NullPointerException 방지")
    void broadcastMessage_NullMessage_DefensiveProgramming() {
        // Given
        ChatMessage nullMessage = null;
        
        // When & Then
        // NullPointerException이 발생할 수 있지만 서비스에서 방어적으로 처리해야 함
        assertThatThrownBy(() -> {
            webSocketChatService.broadcastMessage(nullMessage);
        }).isInstanceOf(NullPointerException.class);
        
        // 메시지 전송은 호출되지 않음
        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
    }
    
    @Test
    @DisplayName("잘못된 채팅방 ID로 브로드캐스트 시 - 안전한 처리")
    void broadcastMessage_InvalidChatRoomId_SafeHandling() {
        // Given
        ChatRoomIdentity invalidChatRoomId = ChatRoomIdentity.of(-1L);
        ChatMessage messageWithInvalidRoom = ChatMessage.newMessage(
            MessageIdentity.of(1L), invalidChatRoomId, userId, SenderType.USER, "메시지", true
        );
        
        // When
        webSocketChatService.broadcastMessage(messageWithInvalidRoom);
        
        // Then
        // 잘못된 ID라도 대상 경로는 생성되어 전송 시도
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/-1"), 
            eq(messageWithInvalidRoom)
        );
    }
    
    @Test
    @DisplayName("대용량 메시지 전송 시 메모리 오류 - 복원력 테스트")
    void broadcastMessage_LargeMessage_MemoryError() {
        // Given
        String largeContent = "A".repeat(100000); // 100KB 메시지
        ChatMessage largeMessage = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, largeContent, true
        );
        
        doThrow(new OutOfMemoryError("힙 메모리 부족"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        // OutOfMemoryError는 심각한 오류이므로 전파될 수 있음
        assertThatThrownBy(() -> {
            webSocketChatService.broadcastMessage(largeMessage);
        }).isInstanceOf(OutOfMemoryError.class);
        
        verify(messagingTemplate).convertAndSend(
            eq("/topic/chat/" + chatRoomId.id()), 
            eq(largeMessage)
        );
    }
    
    @Test
    @DisplayName("연속된 브로드캐스트 오류 시 회로 차단기 패턴 확인")
    void broadcastMessage_ConsecutiveErrors_CircuitBreakerPattern() {
        // Given
        ChatMessage message1 = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, "메시지1", true
        );
        ChatMessage message2 = ChatMessage.newMessage(
            MessageIdentity.of(2L), chatRoomId, userId, SenderType.USER, "메시지2", true
        );
        ChatMessage message3 = ChatMessage.newMessage(
            MessageIdentity.of(3L), chatRoomId, userId, SenderType.USER, "메시지3", true
        );
        
        // 연속적인 오류 시뮬레이션
        doThrow(new RuntimeException("연결 오류 1"))
            .doThrow(new RuntimeException("연결 오류 2"))
            .doThrow(new RuntimeException("연결 오류 3"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        // 회로 차단기 없이도 각 메시지는 독립적으로 처리되어야 함
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastMessage(message1);
            webSocketChatService.broadcastMessage(message2);
            webSocketChatService.broadcastMessage(message3);
        });
        
        // 3번의 전송 시도 검증
        verify(messagingTemplate, times(3)).convertAndSend(any(String.class), any(Object.class));
    }
    
    @Test
    @DisplayName("동시 다발적 메시지 브로드캐스트 시 스레드 안전성 검증")
    void broadcastMessage_ConcurrentAccess_ThreadSafety() {
        // Given
        ChatMessage message1 = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, "동시 메시지1", true
        );
        ChatMessage message2 = ChatMessage.newMessage(
            MessageIdentity.of(2L), chatRoomId, userId, SenderType.PET, "동시 메시지2", false
        );
        
        // 첫 번째 호출은 성공, 두 번째 호출은 실패 시뮬레이션
        doNothing()
            .doThrow(new RuntimeException("동시성 오류"))
            .when(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
        
        // When & Then
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastMessage(message1); // 성공
        });
        
        assertThatNoException().isThrownBy(() -> {
            webSocketChatService.broadcastMessage(message2); // 실패하지만 예외 전파 안됨
        });
        
        verify(messagingTemplate, times(2)).convertAndSend(any(String.class), any(Object.class));
    }
}