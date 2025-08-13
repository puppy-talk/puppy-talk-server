package com.puppy.talk.model.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.MessageIdentity;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.websocket.ChatMessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMessage 도메인 모델 테스트")
class ChatMessageTest {
    
    @Test
    @DisplayName("일반 채팅 메시지 생성 - 성공")
    void createNewMessage_Success() {
        // Given
        MessageIdentity messageId = MessageIdentity.of(1L);
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        String content = "안녕하세요";
        
        // When
        ChatMessage message = ChatMessage.newMessage(
            messageId, chatRoomId, userId, SenderType.USER, content, true
        );
        
        // Then
        assertThat(message.messageId()).isEqualTo(messageId);
        assertThat(message.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(message.userId()).isEqualTo(userId);
        assertThat(message.senderType()).isEqualTo(SenderType.USER);
        assertThat(message.content()).isEqualTo(content);
        assertThat(message.isRead()).isTrue();
        assertThat(message.messageType()).isEqualTo(ChatMessageType.MESSAGE);
        assertThat(message.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("타이핑 시작 메시지 생성 - 성공")
    void createTypingMessage_Success() {
        // Given
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        
        // When
        ChatMessage typingMessage = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        
        // Then
        assertThat(typingMessage.messageId()).isNull();
        assertThat(typingMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(typingMessage.userId()).isEqualTo(userId);
        assertThat(typingMessage.senderType()).isEqualTo(SenderType.USER);
        assertThat(typingMessage.content()).isNull();
        assertThat(typingMessage.isRead()).isFalse();
        assertThat(typingMessage.messageType()).isEqualTo(ChatMessageType.TYPING);
        assertThat(typingMessage.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("타이핑 중단 메시지 생성 - 성공")
    void createStopTypingMessage_Success() {
        // Given
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        
        // When
        ChatMessage stopTypingMessage = ChatMessage.stopTyping(chatRoomId, userId, SenderType.USER);
        
        // Then
        assertThat(stopTypingMessage.messageId()).isNull();
        assertThat(stopTypingMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(stopTypingMessage.userId()).isEqualTo(userId);
        assertThat(stopTypingMessage.senderType()).isEqualTo(SenderType.USER);
        assertThat(stopTypingMessage.content()).isNull();
        assertThat(stopTypingMessage.isRead()).isFalse();
        assertThat(stopTypingMessage.messageType()).isEqualTo(ChatMessageType.STOP_TYPING);
        assertThat(stopTypingMessage.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("읽음 확인 메시지 생성 - 성공")
    void createReadReceiptMessage_Success() {
        // Given
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        MessageIdentity lastReadMessageId = MessageIdentity.of(10L);
        
        // When
        ChatMessage readReceipt = ChatMessage.readReceipt(chatRoomId, userId, lastReadMessageId);
        
        // Then
        assertThat(readReceipt.messageId()).isEqualTo(lastReadMessageId);
        assertThat(readReceipt.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(readReceipt.userId()).isEqualTo(userId);
        assertThat(readReceipt.senderType()).isEqualTo(SenderType.USER);
        assertThat(readReceipt.content()).isNull();
        assertThat(readReceipt.isRead()).isTrue();
        assertThat(readReceipt.messageType()).isEqualTo(ChatMessageType.READ_RECEIPT);
        assertThat(readReceipt.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("커스텀 메시지 생성 - 성공")
    void createCustomMessage_Success() {
        // Given
        MessageIdentity messageId = MessageIdentity.of(1L);
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        String content = "시스템 메시지";
        ChatMessageType messageType = ChatMessageType.SYSTEM;
        
        // When
        ChatMessage customMessage = ChatMessage.of(
            messageId, chatRoomId, userId, SenderType.SYSTEM, content, false, messageType
        );
        
        // Then
        assertThat(customMessage.messageId()).isEqualTo(messageId);
        assertThat(customMessage.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(customMessage.userId()).isEqualTo(userId);
        assertThat(customMessage.senderType()).isEqualTo(SenderType.SYSTEM);
        assertThat(customMessage.content()).isEqualTo(content);
        assertThat(customMessage.isRead()).isFalse();
        assertThat(customMessage.messageType()).isEqualTo(messageType);
        assertThat(customMessage.timestamp()).isNotNull();
    }
    
    @Test
    @DisplayName("타임스탬프 자동 생성 확인")
    void timestampGeneration_Success() {
        // Given
        LocalDateTime before = LocalDateTime.now();
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        
        // When
        ChatMessage message = ChatMessage.typing(chatRoomId, userId, SenderType.USER);
        LocalDateTime after = LocalDateTime.now();
        
        // Then
        assertThat(message.timestamp()).isAfter(before.minusSeconds(1));
        assertThat(message.timestamp()).isBefore(after.plusSeconds(1));
    }
    
    @Test
    @DisplayName("PET 타입 메시지 생성 - 성공")
    void createPetMessage_Success() {
        // Given
        MessageIdentity messageId = MessageIdentity.of(2L);
        ChatRoomIdentity chatRoomId = ChatRoomIdentity.of(1L);
        UserIdentity userId = UserIdentity.of(1L);
        String content = "멍멍! 안녕~";
        
        // When
        ChatMessage petMessage = ChatMessage.newMessage(
            messageId, chatRoomId, userId, SenderType.PET, content, false
        );
        
        // Then
        assertThat(petMessage.senderType()).isEqualTo(SenderType.PET);
        assertThat(petMessage.content()).isEqualTo(content);
        assertThat(petMessage.isRead()).isFalse();
        assertThat(petMessage.messageType()).isEqualTo(ChatMessageType.MESSAGE);
    }
}