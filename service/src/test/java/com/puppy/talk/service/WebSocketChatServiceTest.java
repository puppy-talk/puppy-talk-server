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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketChatService 테스트")
class WebSocketChatServiceTest {
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @InjectMocks
    private WebSocketChatService webSocketChatService;
    
    private ChatRoomIdentity chatRoomId;
    private UserIdentity userId;
    private MessageIdentity messageId;
    
    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomIdentity.of(1L);
        userId = UserIdentity.of(1L);
        messageId = MessageIdentity.of(1L);
    }
    
    @Test
    @DisplayName("채팅 메시지 브로드캐스트 - 성공")
    void broadcastMessage_Success() {
        // Given
        ChatMessage message = ChatMessage.newMessage(
            messageId,
            chatRoomId,
            userId,
            SenderType.USER,
            "안녕하세요",
            true
        );
        
        String expectedDestination = "/topic/chat/" + chatRoomId.id();
        
        // When
        webSocketChatService.broadcastMessage(message);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(message));
    }
    
    @Test
    @DisplayName("타이핑 상태 브로드캐스트 - 성공")
    void broadcastTyping_Success() {
        // Given
        ChatMessage typingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/typing";
        
        // When
        webSocketChatService.broadcastTyping(typingMessage);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(typingMessage));
    }
    
    @Test
    @DisplayName("읽음 확인 브로드캐스트 - 성공")
    void broadcastReadReceipt_Success() {
        // Given
        ChatMessage readMessage = ChatMessage.readReceipt(chatRoomId, userId, messageId);
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/read";
        
        // When
        webSocketChatService.broadcastReadReceipt(readMessage);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(readMessage));
    }
    
    @Test
    @DisplayName("특정 사용자에게 메시지 전송 - 성공")
    void sendToUser_Success() {
        // Given
        ChatMessage privateMessage = ChatMessage.of(
            messageId,
            chatRoomId,
            userId,
            SenderType.SYSTEM,
            "시스템 메시지",
            false,
            ChatMessageType.SYSTEM
        );
        String expectedDestination = "/user/" + userId.id() + "/queue/messages";
        
        // When
        webSocketChatService.sendToUser(userId, privateMessage);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(privateMessage));
    }
    
    @Test
    @DisplayName("시스템 메시지 브로드캐스트 - 성공")
    void broadcastSystemMessage_Success() {
        // Given
        ChatMessage systemMessage = ChatMessage.of(
            null,
            chatRoomId,
            userId,
            SenderType.SYSTEM,
            "사용자가 입장했습니다",
            false,
            ChatMessageType.USER_JOINED
        );
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/system";
        
        // When
        webSocketChatService.broadcastSystemMessage(systemMessage);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(systemMessage));
    }
    
    @Test
    @DisplayName("사용자 입장 알림 - 성공")
    void notifyUserJoined_Success() {
        // Given
        ChatMessage joinMessage = ChatMessage.of(
            null,
            chatRoomId,
            userId,
            SenderType.SYSTEM,
            "사용자가 입장했습니다",
            false,
            ChatMessageType.USER_JOINED
        );
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/system";
        
        // When
        webSocketChatService.notifyUserJoined(joinMessage);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(joinMessage));
    }
    
    @Test
    @DisplayName("사용자 퇴장 알림 - 성공")
    void notifyUserLeft_Success() {
        // Given
        ChatMessage leaveMessage = ChatMessage.of(
            null,
            chatRoomId,
            userId,
            SenderType.SYSTEM,
            "사용자가 퇴장했습니다",
            false,
            ChatMessageType.USER_LEFT
        );
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/system";
        
        // When
        webSocketChatService.notifyUserLeft(leaveMessage);
        
        // Then
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(leaveMessage));
    }
}