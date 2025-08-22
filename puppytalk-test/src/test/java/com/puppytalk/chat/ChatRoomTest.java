package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("채팅방 도메인 테스트")
class ChatRoomTest {
    
    @Test
    @DisplayName("새로운 채팅방을 생성할 수 있다")
    void createChatRoom() {
        // given
        Long userId = 1L;
        PetId petId = PetId.of(1L);
        
        // when
        ChatRoom chatRoom = ChatRoom.create(userId, petId);
        
        // then
        assertThat(chatRoom.getId()).isNotNull();
        assertThat(chatRoom.getUserId()).isEqualTo(userId);
        assertThat(chatRoom.getPetId()).isEqualTo(petId);
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.ACTIVE);
        assertThat(chatRoom.getCreatedAt()).isNotNull();
        assertThat(chatRoom.getLastMessageAt()).isNotNull();
        assertThat(chatRoom.canChat()).isTrue();
    }
    
    @Test
    @DisplayName("채팅방 생성 시 사용자 ID가 null이면 예외가 발생한다")
    void createChatRoomWithNullUserId() {
        // given
        Long userId = null;
        PetId petId = PetId.of(1L);
        
        // when & then
        assertThatThrownBy(() -> ChatRoom.create(userId, petId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 ID는 필수입니다");
    }
    
    @Test
    @DisplayName("채팅방 생성 시 반려동물 ID가 null이면 예외가 발생한다")
    void createChatRoomWithNullPetId() {
        // given
        Long userId = 1L;
        PetId petId = null;
        
        // when & then
        assertThatThrownBy(() -> ChatRoom.create(userId, petId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("반려동물 ID는 필수입니다");
    }
    
    @Test
    @DisplayName("채팅방 생성 시 저장되지 않은 반려동물 ID면 예외가 발생한다")
    void createChatRoomWithNewPetId() {
        // given
        Long userId = 1L;
        PetId petId = PetId.newPet();  // 저장되지 않은 신규 ID
        
        // when & then
        assertThatThrownBy(() -> ChatRoom.create(userId, petId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("저장된 반려동물만 채팅방을 생성할 수 있습니다");
    }
    
    @Test
    @DisplayName("기존 채팅방을 복원할 수 있다")
    void restoreChatRoom() {
        // given
        ChatRoomId id = ChatRoomId.of(1L);
        Long userId = 1L;
        PetId petId = PetId.of(1L);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        ChatRoomStatus status = ChatRoomStatus.ACTIVE;
        LocalDateTime lastMessageAt = LocalDateTime.now().minusHours(1);
        
        // when
        ChatRoom chatRoom = ChatRoom.restore(id, userId, petId, createdAt, status, lastMessageAt);
        
        // then
        assertThat(chatRoom.getId()).isEqualTo(id);
        assertThat(chatRoom.getUserId()).isEqualTo(userId);
        assertThat(chatRoom.getPetId()).isEqualTo(petId);
        assertThat(chatRoom.getCreatedAt()).isEqualTo(createdAt);
        assertThat(chatRoom.getStatus()).isEqualTo(status);
        assertThat(chatRoom.getLastMessageAt()).isEqualTo(lastMessageAt);
    }
    
    @Test
    @DisplayName("채팅방을 활성화할 수 있다")
    void activateChatRoom() {
        // given
        ChatRoom chatRoom = ChatRoom.create(1L, PetId.of(1L));
        chatRoom.deactivate();
        
        // when
        chatRoom.activate();
        
        // then
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.ACTIVE);
        assertThat(chatRoom.canChat()).isTrue();
    }
    
    @Test
    @DisplayName("삭제된 채팅방은 활성화할 수 없다")
    void cannotActivateDeletedChatRoom() {
        // given
        ChatRoom chatRoom = ChatRoom.create(1L, PetId.of(1L));
        chatRoom.delete();
        
        // when & then
        assertThatThrownBy(chatRoom::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제된 채팅방은 활성화할 수 없습니다");
    }
    
    @Test
    @DisplayName("채팅방을 비활성화할 수 있다")
    void deactivateChatRoom() {
        // given
        ChatRoom chatRoom = ChatRoom.create(1L, PetId.of(1L));
        
        // when
        chatRoom.deactivate();
        
        // then
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.INACTIVE);
        assertThat(chatRoom.canChat()).isFalse();
    }
    
    @Test
    @DisplayName("채팅방을 삭제할 수 있다")
    void deleteChatRoom() {
        // given
        ChatRoom chatRoom = ChatRoom.create(1L, PetId.of(1L));
        
        // when
        chatRoom.delete();
        
        // then
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.DELETED);
        assertThat(chatRoom.canChat()).isFalse();
    }
    
    @Test
    @DisplayName("마지막 메시지 시각을 업데이트할 수 있다")
    void updateLastMessageTime() {
        // given
        ChatRoom chatRoom = ChatRoom.create(1L, PetId.of(1L));
        LocalDateTime newMessageTime = LocalDateTime.now().plusMinutes(10);
        
        // when
        chatRoom.updateLastMessageTime(newMessageTime);
        
        // then
        assertThat(chatRoom.getLastMessageAt()).isEqualTo(newMessageTime);
    }
    
    @Test
    @DisplayName("특정 사용자의 채팅방인지 확인할 수 있다")
    void belongsToUser() {
        // given
        Long userId = 1L;
        ChatRoom chatRoom = ChatRoom.create(userId, PetId.of(1L));
        
        // when & then
        assertThat(chatRoom.belongsToUser(userId)).isTrue();
        assertThat(chatRoom.belongsToUser(2L)).isFalse();
    }
    
    @Test
    @DisplayName("특정 반려동물의 채팅방인지 확인할 수 있다")
    void belongsToPet() {
        // given
        PetId petId = PetId.of(1L);
        ChatRoom chatRoom = ChatRoom.create(1L, petId);
        
        // when & then
        assertThat(chatRoom.belongsToPet(petId)).isTrue();
        assertThat(chatRoom.belongsToPet(PetId.of(2L))).isFalse();
    }
    
    @Test
    @DisplayName("마지막 메시지로부터 경과 시간을 확인할 수 있다")
    void getMinutesSinceLastMessage() {
        // given
        ChatRoom chatRoom = ChatRoom.create(1L, PetId.of(1L));
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(30);
        chatRoom.updateLastMessageTime(pastTime);
        
        // when
        long minutes = chatRoom.getMinutesSinceLastMessage();
        
        // then
        assertThat(minutes).isGreaterThanOrEqualTo(29);
        assertThat(minutes).isLessThanOrEqualTo(31);
    }
}