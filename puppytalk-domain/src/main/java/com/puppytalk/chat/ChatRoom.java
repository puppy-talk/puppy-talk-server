package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 채팅방 엔티티
 * 
 * 사용자와 반려동물 간의 1:1 채팅방입니다.
 */
public class ChatRoom {
    
    private final ChatRoomId id;
    private final UserId userId;
    private final PetId petId;
    private final LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    
    private ChatRoom(ChatRoomId id, UserId userId, PetId petId, 
                    LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }
    
    /**
     * 새로운 채팅방 생성 정적 팩토리 메서드
     */
    public static ChatRoom create(UserId userId, PetId petId) {
        validateCreation(userId, petId);
        
        LocalDateTime now = LocalDateTime.now();
        return new ChatRoom(
            ChatRoomId.newChatRoom(),
            userId,
            petId,
            now,
            now
        );
    }
    
    /**
     * 기존 채팅방 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static ChatRoom restore(ChatRoomId id, UserId userId, PetId petId,
                                  LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        validateRestore(id, userId, petId, createdAt, lastMessageAt);
        
        return new ChatRoom(id, userId, petId, createdAt, lastMessageAt);
    }
    
    private static void validateCreation(UserId userId, PetId petId) {
        if (userId == null || !userId.isStored()) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (petId == null || !petId.isStored()) {
            throw new IllegalArgumentException("반려동물 ID는 필수입니다");
        }
    }
    
    private static void validateRestore(ChatRoomId id, UserId userId, PetId petId,
                                      LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 채팅방 ID가 필요합니다");
        }
        validateCreation(userId, petId);
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (lastMessageAt == null) {
            throw new IllegalArgumentException("마지막 메시지 시각은 필수입니다");
        }
    }
    
    /**
     * 메시지가 전송되었을 때 마지막 메시지 시각 업데이트
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }
    
    /**
     * 특정 사용자의 채팅방인지 확인
     */
    public boolean isOwnedBy(UserId userId) {
        return Objects.equals(this.userId, userId);
    }
    
    /**
     * 특정 반려동물의 채팅방인지 확인
     */
    public boolean isForPet(PetId petId) {
        return Objects.equals(this.petId, petId);
    }
    
    /**
     * 특정 사용자와 반려동물의 채팅방인지 확인
     */
    public boolean isRoomFor(UserId userId, PetId petId) {
        return isOwnedBy(userId) && isForPet(petId);
    }
    
    // Getters
    public ChatRoomId getId() { return id; }
    public UserId getUserId() { return userId; }
    public PetId getPetId() { return petId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatRoom other)) return false;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "ChatRoom{" +
                "id=" + id +
                ", userId=" + userId +
                ", petId=" + petId +
                ", createdAt=" + createdAt +
                ", lastMessageAt=" + lastMessageAt +
                '}';
    }
}