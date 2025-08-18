package com.puppy.talk.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.ChatService;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.chat.command.MessageSendCommand;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.chat.Message;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketMessageService 테스트")
class WebSocketMessageServiceTest {
    
    @Mock
    private ChatService chatService;
    
    @Mock
    private RealtimeNotificationPort realtimeNotificationPort;
    
    @InjectMocks
    private WebSocketMessageService webSocketMessageService;
    
    private ChatRoomIdentity chatRoomId;
    private UserIdentity userId;
    private MessageIdentity messageId;
    private String messageContent;
    
    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomIdentity.of(1L);
        userId = UserIdentity.of(1L);
        messageId = MessageIdentity.of(1L);
        messageContent = "안녕하세요";
    }
    
    @Test
    @DisplayName("사용자 메시지 처리 - 성공")
    void processUserMessage_Success() {
        // Given
        Message savedMessage = Message.of(
            messageId,
            chatRoomId,
            SenderType.USER,
            messageContent,
            true,
            LocalDateTime.now()
        );
        MessageSendResult result = new MessageSendResult(savedMessage, null);
        
        when(chatService.sendMessageToPet(eq(chatRoomId), any(MessageSendCommand.class)))
            .thenReturn(result);
        
        // When
        webSocketMessageService.processUserMessage(chatRoomId, userId, messageContent);
        
        // Then
        verify(chatService).sendMessageToPet(eq(chatRoomId), any(MessageSendCommand.class));
        
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastMessage(messageCaptor.capture());
        
        ChatMessage broadcastedMessage = messageCaptor.getValue();
        assertThat(broadcastedMessage.messageId()).isEqualTo(messageId);
        assertThat(broadcastedMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(broadcastedMessage.userId()).isEqualTo(userId);
        assertThat(broadcastedMessage.senderType()).isEqualTo(SenderType.USER);
        assertThat(broadcastedMessage.content()).isEqualTo(messageContent);
        assertThat(broadcastedMessage.isRead()).isTrue();
    }
    
    @Test
    @DisplayName("타이핑 상태 처리 - 타이핑 시작")
    void processTypingStatus_StartTyping() {
        // Given
        boolean isTyping = true;
        
        // When
        webSocketMessageService.processTypingStatus(chatRoomId, userId, isTyping);
        
        // Then
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastTypingStatus(messageCaptor.capture());
        
        ChatMessage typingMessage = messageCaptor.getValue();
        assertThat(typingMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(typingMessage.userId()).isEqualTo(userId);
        assertThat(typingMessage.senderType()).isEqualTo(SenderType.USER);
        assertThat(typingMessage.messageType()).isEqualTo(com.puppy.talk.websocket.ChatMessageType.TYPING);
    }
    
    @Test
    @DisplayName("타이핑 상태 처리 - 타이핑 종료")
    void processTypingStatus_StopTyping() {
        // Given
        boolean isTyping = false;
        
        // When
        webSocketMessageService.processTypingStatus(chatRoomId, userId, isTyping);
        
        // Then
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastTypingStatus(messageCaptor.capture());
        
        ChatMessage typingMessage = messageCaptor.getValue();
        assertThat(typingMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(typingMessage.userId()).isEqualTo(userId);
        assertThat(typingMessage.senderType()).isEqualTo(SenderType.USER);
        assertThat(typingMessage.messageType()).isEqualTo(com.puppy.talk.websocket.ChatMessageType.STOP_TYPING);
    }
    
    @Test
    @DisplayName("읽음 확인 처리 - 성공")
    void processReadReceipt_Success() {
        // When
        webSocketMessageService.processReadReceipt(chatRoomId, userId);
        
        // Then
        verify(chatService).markMessagesAsRead(chatRoomId);
        
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(realtimeNotificationPort).broadcastReadReceipt(messageCaptor.capture());
        
        ChatMessage readReceiptMessage = messageCaptor.getValue();
        assertThat(readReceiptMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(readReceiptMessage.userId()).isEqualTo(userId);
        assertThat(readReceiptMessage.senderType()).isEqualTo(SenderType.USER);
        assertThat(readReceiptMessage.messageType()).isEqualTo(com.puppy.talk.websocket.ChatMessageType.READ_RECEIPT);
        assertThat(readReceiptMessage.messageId()).isNull(); // 전체 읽음 처리이므로 특정 메시지 ID 없음
        assertThat(readReceiptMessage.content()).isNull();
        assertThat(readReceiptMessage.isRead()).isTrue();
    }
    
    @Test
    @DisplayName("MessageSendCommand 생성 검증")
    void processUserMessage_VerifyCommandCreation() {
        // Given
        Message savedMessage = Message.of(
            messageId,
            chatRoomId,
            SenderType.USER,
            messageContent,
            true,
            LocalDateTime.now()
        );
        MessageSendResult result = new MessageSendResult(savedMessage, null);
        
        when(chatService.sendMessageToPet(eq(chatRoomId), any(MessageSendCommand.class)))
            .thenReturn(result);
        
        // When
        webSocketMessageService.processUserMessage(chatRoomId, userId, messageContent);
        
        // Then
        ArgumentCaptor<MessageSendCommand> commandCaptor = ArgumentCaptor.forClass(MessageSendCommand.class);
        verify(chatService).sendMessageToPet(eq(chatRoomId), commandCaptor.capture());
        
        MessageSendCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.content()).isEqualTo(messageContent);
    }
}