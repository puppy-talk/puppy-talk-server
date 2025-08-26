package com.puppytalk.unit.chat;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageId;
import com.puppytalk.chat.MessageType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Message 도메인 엔티티 테스트")
class MessageTest {
    
    @DisplayName("사용자 메시지 생성 - 성공")
    @Test
    void of_UserMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "안녕하세요!";
        LocalDateTime beforeCreate = LocalDateTime.now();
        
        // when
        Message message = Message.of(chatRoomId, content);
        
        // then
        assertNotNull(message);
        assertNull(message.id()); // 아직 저장되지 않음
        assertEquals(chatRoomId, message.chatRoomId());
        assertEquals(MessageType.USER, message.type());
        assertEquals(content, message.content());
        assertNotNull(message.createdAt());
        assertTrue(message.createdAt().isAfter(beforeCreate) || message.createdAt().isEqual(beforeCreate));
        assertTrue(message.isFromUser());
        assertFalse(message.isFromPet());
        assertTrue(message.belongsToChatRoom(chatRoomId));
    }
    
    @DisplayName("사용자 메시지 생성 - 공백 제거")
    @Test
    void of_UserMessage_TrimsWhitespace() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "  안녕하세요!  ";
        
        // when
        Message message = Message.of(chatRoomId, content);
        
        // then
        assertEquals("안녕하세요!", message.content());
    }
    
    @DisplayName("반려동물 메시지 생성 - 성공")
    @Test
    void createPetMessage_Success() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "멍멍! 잘 지내고 있어요!";
        LocalDateTime beforeCreate = LocalDateTime.now();
        
        // when
        Message message = Message.createPetMessage(chatRoomId, content);
        
        // then
        assertNotNull(message);
        assertNull(message.id()); // 아직 저장되지 않음
        assertEquals(chatRoomId, message.chatRoomId());
        assertEquals(MessageType.PET, message.type());
        assertEquals(content, message.content());
        assertNotNull(message.createdAt());
        assertTrue(message.createdAt().isAfter(beforeCreate) || message.createdAt().isEqual(beforeCreate));
        assertFalse(message.isFromUser());
        assertTrue(message.isFromPet());
        assertTrue(message.belongsToChatRoom(chatRoomId));
    }
    
    @DisplayName("반려동물 메시지 생성 - 공백 제거")
    @Test
    void createPetMessage_TrimsWhitespace() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "  멍멍! 잘 지내고 있어요!  ";
        
        // when
        Message message = Message.createPetMessage(chatRoomId, content);
        
        // then
        assertEquals("멍멍! 잘 지내고 있어요!", message.content());
    }
    
    @DisplayName("메시지 생성 - null 채팅방 ID로 실패")
    @Test
    void of_NullChatRoomId_ThrowsException() {
        // given
        ChatRoomId chatRoomId = null;
        String content = "안녕하세요!";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.of(chatRoomId, content)
        );
        
        assertEquals("채팅방 ID는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("메시지 생성 - null 내용으로 실패")
    @Test
    void of_NullContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = null;
        
        // when & then
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Message.of(chatRoomId, content)
        );
        
        assertTrue(exception.getMessage().contains("content"));
    }
    
    @DisplayName("메시지 생성 - 빈 내용으로 실패")
    @Test
    void of_EmptyContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "   ";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.of(chatRoomId, content)
        );
        
        assertEquals("메시지 내용은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("메시지 생성 - 너무 긴 내용으로 실패")
    @Test
    void of_TooLongContent_ThrowsException() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String content = "a".repeat(1001); // 1000자 초과
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.of(chatRoomId, content)
        );
        
        assertEquals("메시지 내용은 1000자를 초과할 수 없습니다", exception.getMessage());
    }
    
    @DisplayName("저장된 메시지 복원 - 성공")
    @Test
    void restore_Success() {
        // given
        MessageId id = MessageId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        MessageType type = MessageType.USER;
        String content = "안녕하세요!";
        LocalDateTime createdAt = LocalDateTime.now();
        
        // when
        Message message = Message.restore(id, chatRoomId, type, content, createdAt);
        
        // then
        assertNotNull(message);
        assertEquals(id, message.id());
        assertEquals(chatRoomId, message.chatRoomId());
        assertEquals(type, message.type());
        assertEquals(content, message.content());
        assertEquals(createdAt, message.createdAt());
    }
    
    @DisplayName("저장된 메시지 복원 - null ID로 실패")
    @Test
    void restore_NullId_ThrowsException() {
        // given
        MessageId id = null;
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        MessageType type = MessageType.USER;
        String content = "안녕하세요!";
        LocalDateTime createdAt = LocalDateTime.now();
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Message.restore(id, chatRoomId, type, content, createdAt)
        );
        
        assertEquals("저장된 메시지 ID가 필요합니다", exception.getMessage());
    }
    
    @DisplayName("사용자 메시지 여부 확인")
    @Test
    void isFromUser_ChecksCorrectly() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        Message userMessage = Message.of(chatRoomId, "사용자 메시지");
        Message petMessage = Message.createPetMessage(chatRoomId, "반려동물 메시지");
        
        // when & then
        assertTrue(userMessage.isFromUser());
        assertFalse(petMessage.isFromUser());
    }
    
    @DisplayName("반려동물 메시지 여부 확인")
    @Test
    void isFromPet_ChecksCorrectly() {
        // given
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        Message userMessage = Message.of(chatRoomId, "사용자 메시지");
        Message petMessage = Message.createPetMessage(chatRoomId, "반려동물 메시지");
        
        // when & then
        assertFalse(userMessage.isFromPet());
        assertTrue(petMessage.isFromPet());
    }
    
    @DisplayName("채팅방 소속 여부 확인")
    @Test
    void belongsToChatRoom_ChecksCorrectly() {
        // given
        ChatRoomId chatRoomId1 = ChatRoomId.from(1L);
        ChatRoomId chatRoomId2 = ChatRoomId.from(2L);
        Message message = Message.of(chatRoomId1, "메시지");
        
        // when & then
        assertTrue(message.belongsToChatRoom(chatRoomId1));
        assertFalse(message.belongsToChatRoom(chatRoomId2));
    }
    
    @DisplayName("equals 메서드 - 동일한 ID면 같은 객체")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        MessageId id = MessageId.from(1L);
        Message message1 = Message.restore(id, ChatRoomId.from(1L), MessageType.USER, "content1", LocalDateTime.now());
        Message message2 = Message.restore(id, ChatRoomId.from(2L), MessageType.PET, "content2", LocalDateTime.now());
        
        // when & then
        assertEquals(message1, message2);
    }
    
    @DisplayName("equals 메서드 - 다른 ID면 다른 객체")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        Message message1 = Message.restore(MessageId.from(1L), ChatRoomId.from(1L), MessageType.USER, "content", LocalDateTime.now());
        Message message2 = Message.restore(MessageId.from(2L), ChatRoomId.from(1L), MessageType.USER, "content", LocalDateTime.now());
        
        // when & then
        assertNotEquals(message1, message2);
    }
    
    @DisplayName("hashCode 메서드 - 동일한 ID면 같은 해시코드")
    @Test
    void hashCode_SameId_ReturnsSameHashCode() {
        // given
        MessageId id = MessageId.from(1L);
        Message message1 = Message.restore(id, ChatRoomId.from(1L), MessageType.USER, "content1", LocalDateTime.now());
        Message message2 = Message.restore(id, ChatRoomId.from(2L), MessageType.PET, "content2", LocalDateTime.now());
        
        // when & then
        assertEquals(message1.hashCode(), message2.hashCode());
    }
}