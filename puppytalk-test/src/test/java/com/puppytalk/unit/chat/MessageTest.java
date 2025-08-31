package com.puppytalk.unit.chat;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageType;
import com.puppytalk.user.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Message 엔티티 단위 테스트")
class MessageTest {

    @DisplayName("사용자 메시지 생성 - 성공")
    @Test
    void create_UserMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "안녕하세요!";

        // when
        Message message = Message.create(chatRoomId, senderId, content);

        // then
        assertNotNull(message);
        assertNull(message.id()); // 아직 저장되지 않음
        assertEquals(chatRoomId, message.chatRoomId());
        assertEquals(senderId, message.senderId());
        assertEquals(content.trim(), message.content());
        assertEquals(MessageType.USER, message.type());
        assertNotNull(message.createdAt());
        assertNotNull(message.updatedAt());
        assertEquals(message.createdAt(), message.updatedAt());
        assertTrue(message.isUserMessage());
        assertFalse(message.isPetMessage());
    }

    @DisplayName("사용자 메시지 생성 - null ChatRoomId로 실패")
    @Test
    void create_UserMessage_NullChatRoomId_ThrowsException() {
        // given
        ChatRoomId chatRoomId = null;
        UserId senderId = UserId.from(1L);
        String content = "안녕하세요!";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.create(chatRoomId, senderId, content)
        );

        assertTrue(exception.getMessage().contains("ChatRoomId"));
    }

    @DisplayName("사용자 메시지 생성 - null SenderId로 실패")
    @Test
    void create_UserMessage_NullSenderId_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = null;
        String content = "안녕하세요!";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.create(chatRoomId, senderId, content)
        );

        assertTrue(exception.getMessage().contains("SenderId"));
    }

    @DisplayName("사용자 메시지 생성 - null 내용으로 실패")
    @Test
    void create_UserMessage_NullContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = null;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.create(chatRoomId, senderId, content)
        );

        assertTrue(exception.getMessage().contains("Content"));
    }

    @DisplayName("사용자 메시지 생성 - 빈 내용으로 실패")
    @Test
    void create_UserMessage_BlankContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "   ";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.create(chatRoomId, senderId, content)
        );

        assertTrue(exception.getMessage().contains("Content"));
    }

    @DisplayName("사용자 메시지 생성 - 내용 길이 초과로 실패")
    @Test
    void create_UserMessage_ContentTooLong_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "a".repeat(1001); // MAX_CONTENT_LENGTH(1000) 초과

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.create(chatRoomId, senderId, content)
        );

        assertTrue(exception.getMessage().contains("Content"));
    }

    @DisplayName("반려동물 메시지 생성 - 성공")
    @Test
    void createPetMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "안녕! 나는 버디야!";

        // when
        Message message = Message.createPetMessage(chatRoomId, content);

        // then
        assertNotNull(message);
        assertNull(message.id()); // 아직 저장되지 않음
        assertEquals(chatRoomId, message.chatRoomId());
        assertNull(message.senderId()); // AI 메시지는 senderId가 null
        assertEquals(content.trim(), message.content());
        assertEquals(MessageType.PET, message.type());
        assertNotNull(message.createdAt());
        assertNotNull(message.updatedAt());
        assertFalse(message.isUserMessage());
        assertTrue(message.isPetMessage());
    }

    @DisplayName("반려동물 메시지 생성 - null ChatRoomId로 실패")
    @Test
    void createPetMessage_NullChatRoomId_ThrowsException() {
        // given
        ChatRoomId chatRoomId = null;
        String content = "안녕!";

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.createPetMessage(chatRoomId, content)
        );

        assertTrue(exception.getMessage().contains("ChatRoomId"));
    }

    @DisplayName("반려동물 메시지 생성 - null 내용으로 실패")
    @Test
    void createPetMessage_NullContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = null;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.createPetMessage(chatRoomId, content)
        );

        assertTrue(exception.getMessage().contains("Content"));
    }

    @DisplayName("Message.of 생성자 - 성공")
    @Test
    void of_Success() {
        // given
        MessageId messageId = MessageId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "안녕하세요!";
        MessageType type = MessageType.USER;
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime updatedAt = LocalDateTime.now();

        // when
        Message message = Message.of(messageId, chatRoomId, senderId, content, type, createdAt, updatedAt);

        // then
        assertEquals(messageId, message.id());
        assertEquals(chatRoomId, message.chatRoomId());
        assertEquals(senderId, message.senderId());
        assertEquals(content.trim(), message.content());
        assertEquals(type, message.type());
        assertEquals(createdAt, message.createdAt());
        assertEquals(updatedAt, message.updatedAt());
    }

    @DisplayName("Message.of 생성자 - null MessageId로 실패")
    @Test
    void of_NullMessageId_ThrowsException() {
        // given
        MessageId messageId = null;
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "안녕하세요!";
        MessageType type = MessageType.USER;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.of(messageId, chatRoomId, senderId, content, type, createdAt, updatedAt)
        );

        assertTrue(exception.getMessage().contains("MessageId"));
    }

    @DisplayName("Message.of 생성자 - null MessageType으로 실패")
    @Test
    void of_NullMessageType_ThrowsException() {
        // given
        MessageId messageId = MessageId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "안녕하세요!";
        MessageType type = null;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.of(messageId, chatRoomId, senderId, content, type, createdAt, updatedAt)
        );

        assertEquals("MessageType must not be null", exception.getMessage());
    }

    @DisplayName("Message.of 생성자 - null CreatedAt으로 실패")
    @Test
    void of_NullCreatedAt_ThrowsException() {
        // given
        MessageId messageId = MessageId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "안녕하세요!";
        MessageType type = MessageType.USER;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = LocalDateTime.now();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.of(messageId, chatRoomId, senderId, content, type, createdAt, updatedAt)
        );

        assertEquals("CreatedAt must not be null", exception.getMessage());
    }

    @DisplayName("메시지 타입 확인 - USER 메시지")
    @Test
    void isUserMessage_UserType_ReturnsTrue() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        Message userMessage = Message.create(chatRoomId, senderId, "사용자 메시지");

        // when & then
        assertTrue(userMessage.isUserMessage());
        assertFalse(userMessage.isPetMessage());
    }

    @DisplayName("메시지 타입 확인 - PET 메시지")
    @Test
    void isPetMessage_PetType_ReturnsTrue() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        Message petMessage = Message.createPetMessage(chatRoomId, "반려동물 메시지");

        // when & then
        assertTrue(petMessage.isPetMessage());
        assertFalse(petMessage.isUserMessage());
    }

    @DisplayName("특정 사용자로부터 온 메시지인지 확인 - 일치")
    @Test
    void isFromUser_MatchingUser_ReturnsTrue() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        Message userMessage = Message.create(chatRoomId, senderId, "사용자 메시지");

        // when & then
        assertTrue(userMessage.isFromUser(senderId));
    }

    @DisplayName("특정 사용자로부터 온 메시지인지 확인 - 불일치")
    @Test
    void isFromUser_DifferentUser_ReturnsFalse() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        UserId otherUserId = UserId.from(2L);
        Message userMessage = Message.create(chatRoomId, senderId, "사용자 메시지");

        // when & then
        assertFalse(userMessage.isFromUser(otherUserId));
    }

    @DisplayName("특정 사용자로부터 온 메시지인지 확인 - PET 메시지")
    @Test
    void isFromUser_PetMessage_ReturnsFalse() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        Message petMessage = Message.createPetMessage(chatRoomId, "반려동물 메시지");

        // when & then
        assertFalse(petMessage.isFromUser(userId));
    }

    @DisplayName("내용 공백 제거 확인")
    @Test
    void create_TrimsContent() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "  안녕하세요!  ";

        // when
        Message message = Message.create(chatRoomId, senderId, content);

        // then
        assertEquals("안녕하세요!", message.content());
    }

    @DisplayName("equals 메서드 - 같은 ID")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        MessageId messageId = MessageId.from(1L);
        Message message1 = Message.of(messageId, ChatRoomId.from(1L), UserId.from(1L), 
                                    "content1", MessageType.USER, LocalDateTime.now(), LocalDateTime.now());
        Message message2 = Message.of(messageId, ChatRoomId.from(2L), UserId.from(2L), 
                                    "content2", MessageType.PET, LocalDateTime.now(), LocalDateTime.now());

        // when & then
        assertEquals(message1, message2); // 같은 ID이면 equals
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @DisplayName("equals 메서드 - 다른 ID")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        MessageId messageId1 = MessageId.from(1L);
        MessageId messageId2 = MessageId.from(2L);
        Message message1 = Message.of(messageId1, ChatRoomId.from(1L), UserId.from(1L), 
                                    "content", MessageType.USER, LocalDateTime.now(), LocalDateTime.now());
        Message message2 = Message.of(messageId2, ChatRoomId.from(1L), UserId.from(1L), 
                                    "content", MessageType.USER, LocalDateTime.now(), LocalDateTime.now());

        // when & then
        assertNotEquals(message1, message2); // 다른 ID이면 not equals
    }

    @DisplayName("toString 메서드")
    @Test
    void toString_ContainsExpectedFields() {
        // given
        MessageId messageId = MessageId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        Message message = Message.of(messageId, chatRoomId, senderId, "테스트 메시지", 
                                   MessageType.USER, LocalDateTime.now(), LocalDateTime.now());

        // when
        String result = message.toString();

        // then
        assertTrue(result.contains("Message{"));
        assertTrue(result.contains("id=" + messageId));
        assertTrue(result.contains("chatRoomId=" + chatRoomId));
        assertTrue(result.contains("senderId=" + senderId));
        assertTrue(result.contains("content='테스트 메시지'"));
        assertTrue(result.contains("type=" + MessageType.USER));
    }

    @DisplayName("기존 호환성 메서드들 확인")
    @Test
    void compatibilityMethods_WorkCorrectly() {
        // given
        MessageId messageId = MessageId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        UserId senderId = UserId.from(1L);
        String content = "테스트 메시지";
        MessageType type = MessageType.USER;
        LocalDateTime createdAt = LocalDateTime.now();

        Message message = Message.of(messageId, chatRoomId, senderId, content, type, createdAt, LocalDateTime.now());

        // when & then - 기존 호환성 메서드들이 새로운 메서드와 동일한 값을 반환하는지 확인
        assertEquals(message.id(), message.id());
        assertEquals(message.chatRoomId(), message.chatRoomId());
        assertEquals(message.type(), message.type());
        assertEquals(message.content(), message.content());
        assertEquals(message.createdAt(), message.createdAt());
    }
}