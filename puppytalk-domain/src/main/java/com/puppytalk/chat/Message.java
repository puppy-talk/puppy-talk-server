package com.puppytalk.chat;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

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

    // Getters (record 스타일)
    public MessageId id() { return id; }
    public ChatRoomId chatRoomId() { return chatRoomId; }
    public UserId senderId() { return senderId; }
    public String content() { return content; }
    public MessageType type() { return type; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message = (Message) o;
        return Objects.equals(id(), message.id()) && Objects.equals(
            chatRoomId(), message.chatRoomId()) && Objects.equals(senderId(),
            message.senderId()) && Objects.equals(content(), message.content())
            && type() == message.type() && Objects.equals(createdAt(),
            message.createdAt()) && Objects.equals(updatedAt(), message.updatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id(), chatRoomId(), senderId(), content(), type(),
            createdAt(), updatedAt());
    }
}