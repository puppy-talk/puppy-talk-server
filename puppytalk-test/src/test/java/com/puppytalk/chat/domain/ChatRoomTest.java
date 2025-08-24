package com.puppytalk.chat.domain;

import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ChatRoom 도메인 테스트")
class ChatRoomTest {

    @Nested
    @DisplayName("채팅방 생성 테스트")
    class ChatRoomCreationTest {

        @Test
        @DisplayName("새 채팅방 생성 - 성공")
        void createChatRoom_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);

            // When
            ChatRoom chatRoom = ChatRoom.create(userId, petId);

            // Then
            assertThat(chatRoom).isNotNull();
            assertThat(chatRoom.id()).isNull(); // 아직 저장되지 않음
            assertThat(chatRoom.userId()).isEqualTo(userId);
            assertThat(chatRoom.petId()).isEqualTo(petId);
            assertThat(chatRoom.createdAt()).isNotNull();
            assertThat(chatRoom.lastMessageAt()).isNotNull();
            assertThat(chatRoom.createdAt()).isEqualTo(chatRoom.lastMessageAt());
        }

        @Test
        @DisplayName("저장된 채팅방 생성 - 성공")
        void createStoredChatRoom_Success() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime lastMessageAt = LocalDateTime.now();

            // When
            ChatRoom chatRoom = ChatRoom.of(chatRoomId, userId, petId, createdAt, lastMessageAt);

            // Then
            assertThat(chatRoom.id()).isEqualTo(chatRoomId);
            assertThat(chatRoom.userId()).isEqualTo(userId);
            assertThat(chatRoom.petId()).isEqualTo(petId);
            assertThat(chatRoom.createdAt()).isEqualTo(createdAt);
            assertThat(chatRoom.lastMessageAt()).isEqualTo(lastMessageAt);
        }
    }

    @Nested
    @DisplayName("채팅방 생성 실패 테스트")
    class ChatRoomCreationFailureTest {

        @Test
        @DisplayName("사용자 ID가 null인 경우 예외 발생")
        void createChatRoom_WithNullUserId_ThrowsException() {
            // Given
            UserId userId = null;
            PetId petId = PetId.of(1L);

            // When & Then
            assertThatThrownBy(() -> ChatRoom.create(userId, petId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 ID는 필수입니다");
        }

        @Test
        @DisplayName("반려동물 ID가 null인 경우 예외 발생")
        void createChatRoom_WithNullPetId_ThrowsException() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = null;

            // When & Then
            assertThatThrownBy(() -> ChatRoom.create(userId, petId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("반려동물 ID는 필수입니다");
        }

        @Test
        @DisplayName("저장된 채팅방 생성 시 ID가 null인 경우 예외 발생")
        void createStoredChatRoom_WithNullId_ThrowsException() {
            // Given
            ChatRoomId chatRoomId = null;
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            LocalDateTime now = LocalDateTime.now();

            // When & Then
            assertThatThrownBy(() -> ChatRoom.of(chatRoomId, userId, petId, now, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장된 채팅방 ID가 필요합니다");
        }

        @Test
        @DisplayName("생성 시각이 null인 경우 예외 발생")
        void createStoredChatRoom_WithNullCreatedAt_ThrowsException() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            LocalDateTime createdAt = null;
            LocalDateTime lastMessageAt = LocalDateTime.now();

            // When & Then
            assertThatThrownBy(() -> ChatRoom.of(chatRoomId, userId, petId, createdAt, lastMessageAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("생성 시각은 필수입니다");
        }
    }

    @Nested
    @DisplayName("채팅방 동작 테스트")
    class ChatRoomBehaviorTest {

        @Test
        @DisplayName("마지막 메시지 시각 업데이트")
        void updateLastMessageTime_Success() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom originalChatRoom = ChatRoom.create(userId, petId);
            LocalDateTime originalTime = originalChatRoom.lastMessageAt();

            // 시간 차이를 보장하기 위한 대기
            try { Thread.sleep(1); } catch (InterruptedException e) { /* ignore */ }

            // When
            ChatRoom updatedChatRoom = originalChatRoom.withLastMessageTime();

            // Then
            assertThat(updatedChatRoom.lastMessageAt()).isAfter(originalTime);
            assertThat(updatedChatRoom.userId()).isEqualTo(originalChatRoom.userId());
            assertThat(updatedChatRoom.petId()).isEqualTo(originalChatRoom.petId());
            assertThat(updatedChatRoom.createdAt()).isEqualTo(originalChatRoom.createdAt());
        }

        @Test
        @DisplayName("채팅방 소유권 확인 - 소유자")
        void isOwnedBy_Owner_ReturnsTrue() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = ChatRoom.create(userId, petId);

            // When
            boolean result = chatRoom.isOwnedBy(userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("채팅방 소유권 확인 - 비소유자")
        void isOwnedBy_NonOwner_ReturnsFalse() {
            // Given
            UserId owner = UserId.of(1L);
            UserId other = UserId.of(2L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom = ChatRoom.create(owner, petId);

            // When
            boolean result = chatRoom.isOwnedBy(other);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("채팅방 동등성 테스트")
    class ChatRoomEqualityTest {

        @Test
        @DisplayName("동일한 ID를 가진 채팅방은 동등하다")
        void equals_SameId_ReturnsTrue() {
            // Given
            ChatRoomId chatRoomId = ChatRoomId.of(1L);
            UserId userId1 = UserId.of(1L);
            UserId userId2 = UserId.of(2L);
            PetId petId = PetId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            
            ChatRoom chatRoom1 = ChatRoom.of(chatRoomId, userId1, petId, now, now);
            ChatRoom chatRoom2 = ChatRoom.of(chatRoomId, userId2, petId, now, now);

            // When & Then
            assertThat(chatRoom1).isEqualTo(chatRoom2);
            assertThat(chatRoom1.hashCode()).isEqualTo(chatRoom2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 채팅방은 동등하지 않다")
        void equals_DifferentId_ReturnsFalse() {
            // Given
            ChatRoomId chatRoomId1 = ChatRoomId.of(1L);
            ChatRoomId chatRoomId2 = ChatRoomId.of(2L);
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            
            ChatRoom chatRoom1 = ChatRoom.of(chatRoomId1, userId, petId, now, now);
            ChatRoom chatRoom2 = ChatRoom.of(chatRoomId2, userId, petId, now, now);

            // When & Then
            assertThat(chatRoom1).isNotEqualTo(chatRoom2);
        }

        @Test
        @DisplayName("ID가 null인 채팅방들은 동등하지 않다")
        void equals_BothNullId_ReturnsFalse() {
            // Given
            UserId userId = UserId.of(1L);
            PetId petId = PetId.of(1L);
            ChatRoom chatRoom1 = ChatRoom.create(userId, petId);
            ChatRoom chatRoom2 = ChatRoom.create(userId, petId);

            // When & Then
            assertThat(chatRoom1).isNotEqualTo(chatRoom2);
        }
    }
}