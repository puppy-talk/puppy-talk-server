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
    
    protected ChatRoomJpaEntity() {}
    
    public ChatRoomJpaEntity(Long userId, Long petId, LocalDateTime lastMessageAt) {
        this.userId = userId;
        this.petId = petId;
        this.lastMessageAt = lastMessageAt;
    }
    
    /**
     * model -> jpa entity
     */
    public static ChatRoomJpaEntity from(ChatRoom chatRoom) {
        return new ChatRoomJpaEntity(
            chatRoom.getUserId().getValue(),
            chatRoom.getPetId().getValue(),
            chatRoom.getLastMessageAt()
        );
    }
    
    /**
     * updateFromDomain 패턴 - 도메인 객체로부터 일괄 업데이트
     * 개별 setter 사용을 방지하여 불변성 보장
     */
    public void update(ChatRoom chatRoom) {
        this.userId = chatRoom.getUserId().getValue();
        this.petId = chatRoom.getPetId().getValue();
        this.lastMessageAt = chatRoom.getLastMessageAt();
    }
    
    /**
     * jpa entity -> model
     */
    public ChatRoom toDomain() {
        return ChatRoom.of(
            ChatRoomId.from(this.id),
            UserId.from(this.userId),
            PetId.from(this.petId),
            this.createdAt,
            this.lastMessageAt
        );
    }
    
    // getter
    public Long getUserId() {
        return userId;
    }
    
    public Long getPetId() {
        return petId;
    }
    
    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }
    
    @Override
    protected Object getId() {
        return id;
    }
}