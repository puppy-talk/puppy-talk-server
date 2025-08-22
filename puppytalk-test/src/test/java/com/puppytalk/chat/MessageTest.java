package com.puppytalk.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("메시지 도메인 테스트")
class MessageTest {
    
    @Test
    @DisplayName("사용자 메시지를 생성할 수 있다")
    void createUserMessage() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "안녕하세요!";
        
        // when
        Message message = Message.createUserMessage(chatRoomId, content);
        
        // then
        assertThat(message.getId()).isNotNull();
        assertThat(message.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(message.getType()).isEqualTo(MessageType.USER);
        assertThat(message.getContent()).isEqualTo(content);
        assertThat(message.getSentAt()).isNotNull();
        assertThat(message.isUserMessage()).isTrue();
        assertThat(message.isPetMessage()).isFalse();
    }
    
    @Test
    @DisplayName("반려동물 메시지를 생성할 수 있다")
    void createPetMessage() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "멍멍! 안녕하세요!";
        
        // when
        Message message = Message.createPetMessage(chatRoomId, content);
        
        // then
        assertThat(message.getId()).isNotNull();
        assertThat(message.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(message.getType()).isEqualTo(MessageType.PET);
        assertThat(message.getContent()).isEqualTo(content);
        assertThat(message.getSentAt()).isNotNull();
        assertThat(message.isUserMessage()).isFalse();
        assertThat(message.isPetMessage()).isTrue();
    }
    
    @Test
    @DisplayName("메시지 생성 시 채팅방 ID가 null이면 예외가 발생한다")
    void createMessageWithNullChatRoomId() {
        // given
        ChatRoomId chatRoomId = null;
        String content = "안녕하세요!";
        
        // when & then
        assertThatThrownBy(() -> Message.createUserMessage(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("채팅방 ID는 필수입니다");
    }
    
    @Test
    @DisplayName("메시지 생성 시 저장되지 않은 채팅방 ID면 예외가 발생한다")
    void createMessageWithNewChatRoomId() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.newChatRoom();  // 저장되지 않은 신규 ID
        String content = "안녕하세요!";
        
        // when & then
        assertThatThrownBy(() -> Message.createUserMessage(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장된 채팅방만 메시지를 생성할 수 있습니다");
    }
    
    @Test
    @DisplayName("메시지 생성 시 내용이 null이면 예외가 발생한다")
    void createMessageWithNullContent() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = null;
        
        // when & then
        assertThatThrownBy(() -> Message.createUserMessage(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지 내용은 필수입니다");
    }
    
    @Test
    @DisplayName("메시지 생성 시 내용이 빈 문자열이면 예외가 발생한다")
    void createMessageWithEmptyContent() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "   ";
        
        // when & then
        assertThatThrownBy(() -> Message.createUserMessage(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지 내용은 필수입니다");
    }
    
    @Test
    @DisplayName("메시지 생성 시 내용이 1000자를 초과하면 예외가 발생한다")
    void createMessageWithTooLongContent() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "a".repeat(1001);
        
        // when & then
        assertThatThrownBy(() -> Message.createUserMessage(chatRoomId, content))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("메시지는 1000자를 초과할 수 없습니다");
    }
    
    @Test
    @DisplayName("메시지 내용의 앞뒤 공백이 제거된다")
    void trimMessageContent() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        String content = "  안녕하세요!  ";
        
        // when
        Message message = Message.createUserMessage(chatRoomId, content);
        
        // then
        assertThat(message.getContent()).isEqualTo("안녕하세요!");
    }
    
    @Test
    @DisplayName("기존 메시지를 복원할 수 있다")
    void restoreMessage() {
        // given
        MessageId id = MessageId.of(1L);
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        MessageType type = MessageType.USER;
        String content = "안녕하세요!";
        LocalDateTime sentAt = LocalDateTime.now().minusHours(1);
        
        // when
        Message message = Message.restore(id, chatRoomId, type, content, sentAt);
        
        // then
        assertThat(message.getId()).isEqualTo(id);
        assertThat(message.getChatRoomId()).isEqualTo(chatRoomId);
        assertThat(message.getType()).isEqualTo(type);
        assertThat(message.getContent()).isEqualTo(content);
        assertThat(message.getSentAt()).isEqualTo(sentAt);
    }
    
    @Test
    @DisplayName("특정 채팅방의 메시지인지 확인할 수 있다")
    void belongsToChatRoom() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        Message message = Message.createUserMessage(chatRoomId, "안녕하세요!");
        
        // when & then
        assertThat(message.belongsToChatRoom(chatRoomId)).isTrue();
        assertThat(message.belongsToChatRoom(ChatRoomId.of(2L))).isFalse();
    }
    
    @Test
    @DisplayName("메시지 전송 후 경과 시간을 확인할 수 있다")
    void getMinutesSinceSent() {
        // given
        MessageId id = MessageId.of(1L);
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(30);
        Message message = Message.restore(id, chatRoomId, MessageType.USER, "안녕하세요!", pastTime);
        
        // when
        long minutes = message.getMinutesSinceSent();
        
        // then
        assertThat(minutes).isGreaterThanOrEqualTo(29);
        assertThat(minutes).isLessThanOrEqualTo(31);
    }
    
    @Test
    @DisplayName("메시지 미리보기를 생성할 수 있다")
    void getPreview() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.of(1L);
        
        // when - 짧은 메시지
        Message shortMessage = Message.createUserMessage(chatRoomId, "안녕하세요!");
        
        // then
        assertThat(shortMessage.getPreview()).isEqualTo("안녕하세요!");
        
        // when - 긴 메시지
        String longContent = "이것은 매우 긴 메시지입니다. 50자를 넘어가는 메시지의 경우 미리보기가 생성됩니다. 테스트를 위해 더 긴 내용을 추가합니다.";
        Message longMessage = Message.createUserMessage(chatRoomId, longContent);
        
        // then
        assertThat(longMessage.getPreview()).hasSize(50);
        assertThat(longMessage.getPreview()).endsWith("...");
    }
}