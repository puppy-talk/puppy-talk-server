package com.puppytalk.chat;

import com.puppytalk.infrastructure.common.BaseEntity;
import jakarta.persistence.*;

/**
 * 메시지 JPA 엔티티
 * 성능 최적화를 위한 복합 인덱스 적용
 */
@Entity
@Table(name = "messages",
       indexes = {
           @Index(name = "idx_messages_chatroom_id_asc", columnList = "chat_room_id, id"),
           @Index(name = "idx_messages_chatroom_created_desc", columnList = "chat_room_id, created_at DESC"),
           @Index(name = "idx_messages_sender_type", columnList = "sender_type")
       })
public class MessageJpaEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 10)
    private MessageType type;
    
    @Column(name = "content", nullable = false, length = 2000)
    private String content;
    
    protected MessageJpaEntity() {
        // JPA 기본 생성자
    }
    
    public MessageJpaEntity(Long chatRoomId, MessageType type, String content) {
        this.chatRoomId = chatRoomId;
        this.type = type;
        this.content = content;
    }
    
    /**
     * model -> jpa entity
     */
    public static MessageJpaEntity from(Message message) {
        return new MessageJpaEntity(
            message.chatRoomId().getValue(),
            message.type(),
            message.content()
        );
    }
    
    /**
     * updateFromDomain 패턴 - 도메인 객체로부터 일괄 업데이트
     * 개별 setter 사용을 방지하여 불변성 보장
     */
    public void update(Message message) {
        this.chatRoomId = message.chatRoomId().getValue();
        this.type = message.type();
        this.content = message.content();
    }
    
    /**
     * jpa entity -> model
     */
    public Message toDomain() {
        return Message.of(
            MessageId.from(this.id),
            ChatRoomId.from(this.chatRoomId),
            null, // senderId - PET 메시지의 경우 null
            this.content,
            this.type,
            this.createdAt,
            this.updatedAt
        );
    }
    
    // getter
    public Long getChatRoomId() {
        return chatRoomId;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String getContent() {
        return content;
    }
    
    @Override
    protected Object getId() {
        return id;
    }
}