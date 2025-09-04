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

        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (lastMessageAt == null) {
            throw new IllegalArgumentException("마지막 메시지 시각은 필수입니다");
        }

        return new ChatRoom(id, userId, petId, createdAt, lastMessageAt);
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

    // getters
    public ChatRoomId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public PetId getPetId() {
        return petId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChatRoom room = (ChatRoom) o;
        return Objects.equals(getId(), room.getId()) && Objects.equals(getUserId(),
            room.getUserId()) && Objects.equals(getPetId(), room.getPetId())
            && Objects.equals(getCreatedAt(), room.getCreatedAt())
            && Objects.equals(getLastMessageAt(), room.getLastMessageAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserId(), getPetId(), getCreatedAt(), getLastMessageAt());
    }
}