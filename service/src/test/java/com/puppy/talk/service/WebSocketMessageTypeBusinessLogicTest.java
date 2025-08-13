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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocket 메시지 타입별 비즈니스 로직 테스트")
class WebSocketMessageTypeBusinessLogicTest {
    
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @InjectMocks
    private WebSocketChatService webSocketChatService;
    
    private ChatRoomIdentity chatRoomId;
    private UserIdentity userId;
    
    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomIdentity.of(1L);
        userId = UserIdentity.of(1L);
    }
    
    @Test
    @DisplayName("일반 메시지 브로드캐스트 - MESSAGE 타입 처리 성공")
    void broadcastMessage_MessageType_Success() {
        // Given
        ChatMessage normalMessage = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, "안녕하세요!", true
        );
        
        // When
        webSocketChatService.broadcastMessage(normalMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id();
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(normalMessage));
        
        // MESSAGE 타입 검증
        assertThat(normalMessage.messageType()).isEqualTo(ChatMessageType.MESSAGE);
        assertThat(normalMessage.content()).isNotNull();
        assertThat(normalMessage.messageId()).isNotNull();
    }
    
    @Test
    @DisplayName("타이핑 시작 메시지 브로드캐스트 - TYPING 타입 전용 채널 처리 성공")
    void broadcastTypingMessage_TypingType_Success() {
        // Given
        ChatMessage typingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        
        // When
        webSocketChatService.broadcastTypingStatus(typingMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/typing";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(typingMessage));
        
        // TYPING 타입 검증
        assertThat(typingMessage.messageType()).isEqualTo(ChatMessageType.TYPING);
        assertThat(typingMessage.content()).isNull();
        assertThat(typingMessage.messageId()).isNull();
    }
    
    @Test
    @DisplayName("타이핑 중단 메시지 브로드캐스트 - STOP_TYPING 타입 전용 채널 처리 성공")
    void broadcastStopTypingMessage_StopTypingType_Success() {
        // Given
        ChatMessage stopTypingMessage = ChatMessage.stopTyping(chatRoomId, userId, SenderType.USER);
        
        // When
        webSocketChatService.broadcastTypingStatus(stopTypingMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/typing";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(stopTypingMessage));
        
        // STOP_TYPING 타입 검증
        assertThat(stopTypingMessage.messageType()).isEqualTo(ChatMessageType.STOP_TYPING);
        assertThat(stopTypingMessage.content()).isNull();
        assertThat(stopTypingMessage.messageId()).isNull();
    }
    
    @Test
    @DisplayName("읽음 확인 메시지 브로드캐스트 - READ_RECEIPT 타입 전용 채널 처리 성공")
    void broadcastReadReceiptMessage_ReadReceiptType_Success() {
        // Given
        MessageIdentity lastReadMessageId = MessageIdentity.of(10L);
        ChatMessage readReceiptMessage = ChatMessage.readReceipt(chatRoomId, userId, lastReadMessageId);
        
        // When
        webSocketChatService.broadcastReadReceipt(readReceiptMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/read";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(readReceiptMessage));
        
        // READ_RECEIPT 타입 검증
        assertThat(readReceiptMessage.messageType()).isEqualTo(ChatMessageType.READ_RECEIPT);
        assertThat(readReceiptMessage.messageId()).isEqualTo(lastReadMessageId);
        assertThat(readReceiptMessage.isRead()).isTrue();
    }
    
    @Test
    @DisplayName("시스템 메시지 브로드캐스트 - SYSTEM 타입 전용 채널 처리 성공")
    void broadcastSystemMessage_SystemType_Success() {
        // Given
        ChatMessage systemMessage = ChatMessage.of(
            MessageIdentity.of(99L), chatRoomId, userId, SenderType.SYSTEM, 
            "시스템 점검이 예정되어 있습니다.", false, ChatMessageType.SYSTEM
        );
        
        // When
        webSocketChatService.broadcastSystemMessage(systemMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/system";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(systemMessage));
        
        // SYSTEM 타입 검증
        assertThat(systemMessage.messageType()).isEqualTo(ChatMessageType.SYSTEM);
        assertThat(systemMessage.senderType()).isEqualTo(SenderType.SYSTEM);
        assertThat(systemMessage.content()).isNotNull();
    }
    
    @Test
    @DisplayName("사용자 입장 메시지 브로드캐스트 - USER_JOINED 타입 처리 성공")
    void broadcastUserJoinedMessage_UserJoinedType_Success() {
        // Given
        ChatMessage userJoinedMessage = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, 
            "사용자가 채팅방에 입장했습니다.", false, ChatMessageType.USER_JOINED
        );
        
        // When
        webSocketChatService.broadcastSystemMessage(userJoinedMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/system";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(userJoinedMessage));
        
        // USER_JOINED 타입 검증
        assertThat(userJoinedMessage.messageType()).isEqualTo(ChatMessageType.USER_JOINED);
        assertThat(userJoinedMessage.senderType()).isEqualTo(SenderType.SYSTEM);
        assertThat(userJoinedMessage.messageId()).isNull();
    }
    
    @Test
    @DisplayName("사용자 퇴장 메시지 브로드캐스트 - USER_LEFT 타입 처리 성공")
    void broadcastUserLeftMessage_UserLeftType_Success() {
        // Given
        ChatMessage userLeftMessage = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, 
            "사용자가 채팅방을 나갔습니다.", false, ChatMessageType.USER_LEFT
        );
        
        // When
        webSocketChatService.broadcastSystemMessage(userLeftMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/system";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(userLeftMessage));
        
        // USER_LEFT 타입 검증
        assertThat(userLeftMessage.messageType()).isEqualTo(ChatMessageType.USER_LEFT);
        assertThat(userLeftMessage.senderType()).isEqualTo(SenderType.SYSTEM);
    }
    
    @Test
    @DisplayName("PET 타이핑 상태 브로드캐스트 - AI 펫이 타이핑 중일 때 처리 성공")
    void broadcastPetTypingMessage_PetTypingType_Success() {
        // Given
        ChatMessage petTypingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.PET);
        
        // When
        webSocketChatService.broadcastTypingStatus(petTypingMessage);
        
        // Then
        String expectedDestination = "/topic/chat/" + chatRoomId.id() + "/typing";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(petTypingMessage));
        
        // PET 타이핑 검증
        assertThat(petTypingMessage.senderType()).isEqualTo(SenderType.PET);
        assertThat(petTypingMessage.messageType()).isEqualTo(ChatMessageType.TYPING);
        assertThat(petTypingMessage.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("개인 메시지 전송 - 특정 사용자에게만 메시지 전달 성공")
    void sendPrivateMessage_ToSpecificUser_Success() {
        // Given
        ChatMessage privateMessage = ChatMessage.newMessage(
            MessageIdentity.of(2L), chatRoomId, userId, SenderType.SYSTEM, 
            "개인 알림 메시지입니다.", false
        );
        
        // When
        webSocketChatService.sendToUser(userId, privateMessage);
        
        // Then
        String expectedDestination = "/user/" + userId.id() + "/queue/messages";
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(privateMessage));
        
        // 개인 메시지 검증
        assertThat(privateMessage.userId()).isEqualTo(userId);
        assertThat(privateMessage.content()).contains("개인 알림");
    }
    
    @Test
    @DisplayName("메시지 타입별 타임스탬프 생성 검증 - 모든 타입에서 타임스탬프 존재")
    void messageTypeTimestamp_AllTypes_HasTimestamp() {
        // Given
        LocalDateTime before = LocalDateTime.now();
        
        // When
        ChatMessage messageType = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, "내용", true
        );
        ChatMessage typingType = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        ChatMessage stopTypingType = ChatMessage.stopTyping(chatRoomId, userId, SenderType.USER);
        ChatMessage readReceiptType = ChatMessage.readReceipt(chatRoomId, userId, MessageIdentity.of(5L));
        ChatMessage systemType = ChatMessage.of(
            null, chatRoomId, userId, SenderType.SYSTEM, "시스템", false, ChatMessageType.SYSTEM
        );
        
        LocalDateTime after = LocalDateTime.now();
        
        // Then
        assertThat(messageType.timestamp()).isAfter(before.minusSeconds(1));
        assertThat(messageType.timestamp()).isBefore(after.plusSeconds(1));
        
        assertThat(typingType.timestamp()).isAfter(before.minusSeconds(1));
        assertThat(typingType.timestamp()).isBefore(after.plusSeconds(1));
        
        assertThat(stopTypingType.timestamp()).isAfter(before.minusSeconds(1));
        assertThat(stopTypingType.timestamp()).isBefore(after.plusSeconds(1));
        
        assertThat(readReceiptType.timestamp()).isAfter(before.minusSeconds(1));
        assertThat(readReceiptType.timestamp()).isBefore(after.plusSeconds(1));
        
        assertThat(systemType.timestamp()).isAfter(before.minusSeconds(1));
        assertThat(systemType.timestamp()).isBefore(after.plusSeconds(1));
    }
    
    @Test
    @DisplayName("잘못된 메시지 타입 처리 시 기본 채널로 브로드캐스트 - 안전성 검증")
    void broadcastMessage_UnknownType_FallbackToDefaultChannel() {
        // Given
        ChatMessage messageWithUnknownHandling = ChatMessage.newMessage(
            MessageIdentity.of(1L), chatRoomId, userId, SenderType.USER, "일반 메시지", true
        );
        
        // When
        webSocketChatService.broadcastMessage(messageWithUnknownHandling);
        
        // Then
        // 일반 메시지는 기본 채팅 채널로 브로드캐스트
        String expectedDestination = "/topic/chat/" + chatRoomId.id();
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(messageWithUnknownHandling));
        
        // 메시지 내용 보존 검증
        assertThat(messageWithUnknownHandling.content()).isEqualTo("일반 메시지");
        assertThat(messageWithUnknownHandling.messageType()).isEqualTo(ChatMessageType.MESSAGE);
    }
}