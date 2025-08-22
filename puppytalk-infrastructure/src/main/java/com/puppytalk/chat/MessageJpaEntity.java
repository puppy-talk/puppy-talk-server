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
           @Index(name = "idx_messages_type", columnList = "type")
       })
public class MessageJpaEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
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
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getChatRoomId() {
        return chatRoomId;
    }
    
    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}