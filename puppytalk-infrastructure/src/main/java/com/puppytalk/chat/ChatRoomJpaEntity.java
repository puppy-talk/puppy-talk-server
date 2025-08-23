package com.puppytalk.chat;

import com.puppytalk.infrastructure.common.BaseEntity;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 채팅방 JPA 엔티티
 */
@Entity
@Table(name = "chat_rooms",
       indexes = {
           @Index(name = "idx_chat_rooms_user_id", columnList = "user_id"),
           @Index(name = "idx_chat_rooms_pet_id", columnList = "pet_id"),
           @Index(name = "uk_chat_rooms_user_pet", columnList = "user_id, pet_id", unique = true)
       })
public class ChatRoomJpaEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "pet_id", nullable = false)
    private Long petId;
    
    @Column(name = "last_message_at", nullable = false)
    private LocalDateTime lastMessageAt;
    
    protected ChatRoomJpaEntity() {
        // JPA 기본 생성자
    }
    
    public ChatRoomJpaEntity(Long userId, Long petId, LocalDateTime lastMessageAt) {
        this.userId = userId;
        this.petId = petId;
        this.lastMessageAt = lastMessageAt;
    }
    
    /**
     * JPA Entity를 Domain Model로 변환하는 정적 팩토리 메서드
     */
    public static ChatRoom toModel(ChatRoomJpaEntity entity) {
        return ChatRoom.of(
            ChatRoomId.from(entity.getId()),
            UserId.from(entity.getUserId()),
            PetId.from(entity.getPetId()),
            entity.getCreatedAt(),
            entity.getLastMessageAt()
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getPetId() {
        return petId;
    }
    
    public void setPetId(Long petId) {
        this.petId = petId;
    }
    
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
    
    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}