package com.puppytalk.chat;

import com.puppytalk.pet.PetId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 채팅방 엔티티
 * 
 * 반려동물과 사용자 간의 1:1 채팅방입니다.
 * 하나의 반려동물당 하나의 채팅방만 존재합니다.
 */
public class ChatRoom {
    
    private final ChatRoomId id;
    private final Long userId;  // 사용자 ID
    private final PetId petId;  // 반려동물 ID (1:1 관계)
    private final LocalDateTime createdAt;
    private ChatRoomStatus status;
    private LocalDateTime lastMessageAt;  // 마지막 메시지 시각
    
    private ChatRoom(ChatRoomId id, Long userId, PetId petId, LocalDateTime createdAt, 
                    ChatRoomStatus status, LocalDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.createdAt = createdAt;
        this.status = status;
        this.lastMessageAt = lastMessageAt;
    }
    
    /**
     * 새로운 채팅방 생성 정적 팩토리 메서드
     */
    public static ChatRoom create(Long userId, PetId petId) {
        validateCreation(userId, petId);
        
        LocalDateTime now = LocalDateTime.now();
        return new ChatRoom(
            ChatRoomId.newChatRoom(),
            userId,
            petId,
            now,
            ChatRoomStatus.ACTIVE,
            now  // 생성 시점을 마지막 메시지 시각으로 설정
        );
    }
    
    /**
     * 기존 채팅방 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static ChatRoom restore(ChatRoomId id, Long userId, PetId petId, LocalDateTime createdAt,
                                  ChatRoomStatus status, LocalDateTime lastMessageAt) {
        validateRestore(id, userId, petId, createdAt, status);
        
        return new ChatRoom(id, userId, petId, createdAt, status, lastMessageAt);
    }
    
    private static void validateCreation(Long userId, PetId petId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (petId == null) {
            throw new IllegalArgumentException("반려동물 ID는 필수입니다");
        }
        if (!petId.isStored()) {
            throw new IllegalArgumentException("저장된 반려동물만 채팅방을 생성할 수 있습니다");
        }
    }
    
    private static void validateRestore(ChatRoomId id, Long userId, PetId petId, 
                                       LocalDateTime createdAt, ChatRoomStatus status) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 채팅방 ID가 필요합니다");
        }
        validateCreation(userId, petId);
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("채팅방 상태는 필수입니다");
        }
    }
    
    /**
     * 채팅방 활성화
     */
    public void activate() {
        if (status == ChatRoomStatus.DELETED) {
            throw new IllegalStateException("삭제된 채팅방은 활성화할 수 없습니다");
        }
        this.status = ChatRoomStatus.ACTIVE;
    }
    
    /**
     * 채팅방 비활성화
     */
    public void deactivate() {
        if (status == ChatRoomStatus.DELETED) {
            throw new IllegalStateException("삭제된 채팅방은 비활성화할 수 없습니다");
        }
        this.status = ChatRoomStatus.INACTIVE;
    }
    
    /**
     * 채팅방 삭제 (소프트 삭제)
     */
    public void delete() {
        this.status = ChatRoomStatus.DELETED;
    }
    
    /**
     * 마지막 메시지 시각 업데이트
     */
    public void updateLastMessageTime(LocalDateTime messageTime) {
        if (messageTime == null) {
            throw new IllegalArgumentException("메시지 시각은 필수입니다");
        }
        this.lastMessageAt = messageTime;
    }
    
    /**
     * 채팅 가능한 상태인지 확인
     */
    public boolean canChat() {
        return status.isChatAvailable();
    }
    
    /**
     * 특정 사용자의 채팅방인지 확인
     */
    public boolean belongsToUser(Long userId) {
        return Objects.equals(this.userId, userId);
    }
    
    /**
     * 특정 반려동물의 채팅방인지 확인
     */
    public boolean belongsToPet(PetId petId) {
        return Objects.equals(this.petId, petId);
    }
    
    /**
     * 마지막 활동 시간으로부터 경과 시간 확인 (분 단위)
     */
    public long getMinutesSinceLastMessage() {
        if (lastMessageAt == null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        }
        return java.time.Duration.between(lastMessageAt, LocalDateTime.now()).toMinutes();
    }
    
    // Getters
    public ChatRoomId getId() { return id; }
    public Long getUserId() { return userId; }
    public PetId getPetId() { return petId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public ChatRoomStatus getStatus() { return status; }
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
                ", status=" + status +
                '}';
    }
}