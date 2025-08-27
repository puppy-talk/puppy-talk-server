package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import com.puppytalk.support.validation.Preconditions;
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
    private final LocalDateTime lastMessageAt;

    private ChatRoom(ChatRoomId id, UserId userId, PetId petId,
                     LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    public static ChatRoom create(UserId userId, PetId petId) {
        validateUserId(userId);
        validatePetId(petId);
        
        LocalDateTime now = LocalDateTime.now();
        return new ChatRoom(
            ChatRoomId.create(),
            userId,
            petId,
            now,
            now
        );
    }

    public static ChatRoom of(
        ChatRoomId id,
        UserId userId,
        PetId petId,
        LocalDateTime createdAt,
        LocalDateTime lastMessageAt
    ) {
        Preconditions.requireValidId(id, "ChatRoomId");
        validateUserId(userId);
        validatePetId(petId);
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (lastMessageAt == null) {
            throw new IllegalArgumentException("마지막 메시지 시각은 필수입니다");
        }

        return new ChatRoom(id, userId, petId, createdAt, lastMessageAt);
    }

    private static void validateUserId(UserId userId) {
        Preconditions.requireValidId(userId, "UserId");
    }
    
    private static void validatePetId(PetId petId) {
        Preconditions.requireValidId(petId, "PetId");
    }

    /**
     * 메시지가 전송되었을 때 마지막 메시지 시각 업데이트
     */
    public ChatRoom withLastMessageTime() {
        return new ChatRoom(id, userId, petId, createdAt, LocalDateTime.now());
    }

    /**
     * 특정 사용자의 채팅방인지 확인
     */
    public boolean isOwnedBy(UserId userId) {
        return Objects.equals(this.userId, userId);
    }

    // getter
    public ChatRoomId id() { return id; }
    public UserId userId() { return userId; }
    public PetId petId() { return petId; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime lastMessageAt() { return lastMessageAt; }

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