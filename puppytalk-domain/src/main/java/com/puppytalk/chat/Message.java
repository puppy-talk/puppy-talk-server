package com.puppytalk.chat;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 채팅 메시지 도메인 모델
 */
public class Message {
    public static final int MAX_CONTENT_LENGTH = 1000;
    
    private final MessageId id;
    private final ChatRoomId chatRoomId;
    private final UserId senderId;
    private final String content;
    private final MessageType type;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Message(MessageId id, ChatRoomId chatRoomId, UserId senderId, String content, 
                   MessageType type, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 사용자 메시지 생성
     */
    public static Message create(ChatRoomId chatRoomId, UserId senderId, String content) {
        Preconditions.requireValidId(chatRoomId, "ChatRoomId");
        Preconditions.requireValidId(senderId, "SenderId");
        Preconditions.requireNonBlank(content, "Content", MAX_CONTENT_LENGTH);
        
        String validContent = content.trim();
        LocalDateTime now = LocalDateTime.now();
        return new Message(null, chatRoomId, senderId, validContent, MessageType.USER, now, now);
    }

    /**
     * AI 메시지 생성
     */
    public static Message createPetMessage(ChatRoomId chatRoomId, String content) {
        Preconditions.requireValidId(chatRoomId, "ChatRoomId");
        Preconditions.requireNonBlank(content, "Content", MAX_CONTENT_LENGTH);
        
        String validContent = content.trim();
        LocalDateTime now = LocalDateTime.now();
        return new Message(null, chatRoomId, null, validContent, MessageType.PET, now, now);
    }

    /**
     * 기존 메시지 데이터로부터 객체 생성
     */
    public static Message of(MessageId id, ChatRoomId chatRoomId, UserId senderId, 
                                String content, MessageType type, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Preconditions.requireValidId(id, "MessageId");
        Preconditions.requireValidId(chatRoomId, "ChatRoomId");
        Preconditions.requireNonBlank(content, "Content", MAX_CONTENT_LENGTH);
        String validContent = content.trim();
        if (type == null) {
            throw new IllegalArgumentException("MessageType must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt must not be null");
        }

        return new Message(id, chatRoomId, senderId, validContent, type, createdAt, updatedAt);
    }

    // Getters
    public MessageId getId() { return id; }
    public ChatRoomId getChatRoomId() { return chatRoomId; }
    public UserId getSenderId() { return senderId; }
    public String getContent() { return content; }
    public MessageType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Business methods
    public boolean isUserMessage() {
        return type == MessageType.USER;
    }

    public boolean isPetMessage() {
        return type == MessageType.PET;
    }

    public boolean isFromUser(UserId userId) {
        return isUserMessage() && Objects.equals(senderId, userId);
    }

    // 기존 호환성을 위한 메서드들
    public MessageId id() { return id; }
    public ChatRoomId chatRoomId() { return chatRoomId; }
    public MessageType type() { return type; }
    public String content() { return content; }
    public LocalDateTime createdAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatRoomId=" + chatRoomId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", createdAt=" + createdAt +
                '}';
    }
}