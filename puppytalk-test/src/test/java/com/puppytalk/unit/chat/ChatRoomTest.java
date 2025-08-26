package com.puppytalk.unit.chat;

import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatRoom 도메인 엔티티 테스트")
class ChatRoomTest {
    
    @DisplayName("채팅방 생성 - 성공")
    @Test
    void create_Success() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        LocalDateTime beforeCreate = LocalDateTime.now();
        
        // when
        ChatRoom chatRoom = ChatRoom.create(userId, petId);
        
        // then
        assertNotNull(chatRoom);
        assertNotNull(chatRoom.id());
        assertEquals(userId, chatRoom.userId());
        assertEquals(petId, chatRoom.petId());
        assertNotNull(chatRoom.createdAt());
        assertNotNull(chatRoom.lastMessageAt());
        assertTrue(chatRoom.createdAt().isAfter(beforeCreate) || chatRoom.createdAt().isEqual(beforeCreate));
        assertTrue(chatRoom.lastMessageAt().isAfter(beforeCreate) || chatRoom.lastMessageAt().isEqual(beforeCreate));
        assertEquals(chatRoom.createdAt(), chatRoom.lastMessageAt()); // 생성시 같은 시간
        assertTrue(chatRoom.isOwnedBy(userId));
    }
    
    @DisplayName("채팅방 생성 - null 사용자 ID로 실패")
    @Test
    void create_NullUserId_ThrowsException() {
        // given
        UserId userId = null;
        PetId petId = PetId.from(1L);
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ChatRoom.create(userId, petId)
        );
        
        assertEquals("사용자 ID는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("채팅방 생성 - 저장되지 않은 사용자 ID로 실패")
    @Test
    void create_NotStoredUserId_ThrowsException() {
        // given
        UserId userId = UserId.create(); // 저장되지 않은 ID
        PetId petId = PetId.from(1L);
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ChatRoom.create(userId, petId)
        );
        
        assertEquals("사용자 ID는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("채팅방 생성 - null 반려동물 ID로 실패")
    @Test
    void create_NullPetId_ThrowsException() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ChatRoom.create(userId, petId)
        );
        
        assertEquals("반려동물 ID는 필수입니다", exception.getMessage());
    }
    
    @DisplayName("저장된 채팅방 객체 생성 - 성공")
    @Test
    void of_Success() {
        // given
        ChatRoomId id = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime lastMessageAt = LocalDateTime.now();
        
        // when
        ChatRoom chatRoom = ChatRoom.of(id, userId, petId, createdAt, lastMessageAt);
        
        // then
        assertNotNull(chatRoom);
        assertEquals(id, chatRoom.id());
        assertEquals(userId, chatRoom.userId());
        assertEquals(petId, chatRoom.petId());
        assertEquals(createdAt, chatRoom.createdAt());
        assertEquals(lastMessageAt, chatRoom.lastMessageAt());
    }
    
    @DisplayName("저장된 채팅방 객체 생성 - null ID로 실패")
    @Test
    void of_NullId_ThrowsException() {
        // given
        ChatRoomId id = null;
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastMessageAt = LocalDateTime.now();
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ChatRoom.of(id, userId, petId, createdAt, lastMessageAt)
        );
        
        assertEquals("저장된 채팅방 ID가 필요합니다", exception.getMessage());
    }
    
    @DisplayName("저장된 채팅방 객체 생성 - null 생성 시각으로 실패")
    @Test
    void of_NullCreatedAt_ThrowsException() {
        // given
        ChatRoomId id = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        LocalDateTime createdAt = null;
        LocalDateTime lastMessageAt = LocalDateTime.now();
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ChatRoom.of(id, userId, petId, createdAt, lastMessageAt)
        );
        
        assertEquals("생성 시각은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("저장된 채팅방 객체 생성 - null 마지막 메시지 시각으로 실패")
    @Test
    void of_NullLastMessageAt_ThrowsException() {
        // given
        ChatRoomId id = ChatRoomId.from(1L);
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastMessageAt = null;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ChatRoom.of(id, userId, petId, createdAt, lastMessageAt)
        );
        
        assertEquals("마지막 메시지 시각은 필수입니다", exception.getMessage());
    }
    
    @DisplayName("마지막 메시지 시각 업데이트 - 성공")
    @Test
    void withLastMessageTime_Success() {
        // given
        ChatRoom originalChatRoom = ChatRoom.of(
            ChatRoomId.from(1L),
            UserId.from(1L),
            PetId.from(1L),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1)
        );
        LocalDateTime beforeUpdate = LocalDateTime.now();
        
        // when
        ChatRoom updatedChatRoom = originalChatRoom.withLastMessageTime();
        
        // then
        assertNotNull(updatedChatRoom);
        assertEquals(originalChatRoom.id(), updatedChatRoom.id());
        assertEquals(originalChatRoom.userId(), updatedChatRoom.userId());
        assertEquals(originalChatRoom.petId(), updatedChatRoom.petId());
        assertEquals(originalChatRoom.createdAt(), updatedChatRoom.createdAt());
        assertNotEquals(originalChatRoom.lastMessageAt(), updatedChatRoom.lastMessageAt());
        assertTrue(updatedChatRoom.lastMessageAt().isAfter(beforeUpdate) || 
                  updatedChatRoom.lastMessageAt().isEqual(beforeUpdate));
    }
    
    @DisplayName("소유권 확인 - 올바른 소유자")
    @Test
    void isOwnedBy_CorrectOwner_ReturnsTrue() {
        // given
        UserId userId = UserId.from(1L);
        ChatRoom chatRoom = ChatRoom.create(userId, PetId.from(1L));
        
        // when & then
        assertTrue(chatRoom.isOwnedBy(userId));
    }
    
    @DisplayName("소유권 확인 - 잘못된 소유자")
    @Test
    void isOwnedBy_WrongOwner_ReturnsFalse() {
        // given
        UserId ownerId = UserId.from(1L);
        UserId otherUserId = UserId.from(2L);
        ChatRoom chatRoom = ChatRoom.create(ownerId, PetId.from(1L));
        
        // when & then
        assertFalse(chatRoom.isOwnedBy(otherUserId));
    }
    
    @DisplayName("equals 메서드 - 동일한 ID면 같은 객체")
    @Test
    void equals_SameId_ReturnsTrue() {
        // given
        ChatRoomId id = ChatRoomId.from(1L);
        ChatRoom chatRoom1 = ChatRoom.of(id, UserId.from(1L), PetId.from(1L), LocalDateTime.now(), LocalDateTime.now());
        ChatRoom chatRoom2 = ChatRoom.of(id, UserId.from(2L), PetId.from(2L), LocalDateTime.now(), LocalDateTime.now());
        
        // when & then
        assertEquals(chatRoom1, chatRoom2);
    }
    
    @DisplayName("equals 메서드 - 다른 ID면 다른 객체")
    @Test
    void equals_DifferentId_ReturnsFalse() {
        // given
        ChatRoom chatRoom1 = ChatRoom.of(ChatRoomId.from(1L), UserId.from(1L), PetId.from(1L), LocalDateTime.now(), LocalDateTime.now());
        ChatRoom chatRoom2 = ChatRoom.of(ChatRoomId.from(2L), UserId.from(1L), PetId.from(1L), LocalDateTime.now(), LocalDateTime.now());
        
        // when & then
        assertNotEquals(chatRoom1, chatRoom2);
    }
    
    @DisplayName("hashCode 메서드 - 동일한 ID면 같은 해시코드")
    @Test
    void hashCode_SameId_ReturnsSameHashCode() {
        // given
        ChatRoomId id = ChatRoomId.from(1L);
        ChatRoom chatRoom1 = ChatRoom.of(id, UserId.from(1L), PetId.from(1L), LocalDateTime.now(), LocalDateTime.now());
        ChatRoom chatRoom2 = ChatRoom.of(id, UserId.from(2L), PetId.from(2L), LocalDateTime.now(), LocalDateTime.now());
        
        // when & then
        assertEquals(chatRoom1.hashCode(), chatRoom2.hashCode());
    }
}