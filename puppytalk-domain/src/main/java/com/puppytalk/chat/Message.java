package com.puppytalk.chat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 채팅 메시지 엔티티
 */
public class Message {
    private final MessageId id;
    private final ChatRoomId chatRoomId;
    private final MessageType type;
    private final String content;
    private final LocalDateTime createdAt;

    private Message(MessageId id, ChatRoomId chatRoomId, MessageType type,
                   String content, LocalDateTime createdAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Message of(ChatRoomId chatRoomId, String content) {
        validateChatRoomId(chatRoomId);
        validateContent(content);
        
        return new Message(
            null,
            chatRoomId,
            MessageType.USER,
            content.trim(),
            LocalDateTime.now()
        );
    }

    public static Message createPetMessage(ChatRoomId chatRoomId, String content) {
        validateChatRoomId(chatRoomId);
        validateContent(content);
        
        return new Message(
            null,
            chatRoomId,
            MessageType.PET,
            content.trim(),
            LocalDateTime.now()
        );
    }

    public static Message restore(MessageId id, ChatRoomId chatRoomId, MessageType type,
                                  String content, LocalDateTime createdAt) {
        if (id == null || !id.isValid()) {
            throw new IllegalArgumentException("저장된 메시지 ID가 필요합니다");
        }
        validateChatRoomId(chatRoomId);
        if (type == null) {
            throw new IllegalArgumentException("메시지 타입은 필수입니다");
        }
        validateContent(content);
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }

        return new Message(id, chatRoomId, type, content, createdAt);
    }

    private static void validateChatRoomId(ChatRoomId chatRoomId) {
        if (chatRoomId == null || !chatRoomId.isValid()) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다");
        }
    }
    
    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다");
        }
        if (content.trim().length() > 1000) {
            throw new IllegalArgumentException("메시지 내용은 1000자를 초과할 수 없습니다");
        }
    }

    /**
     * 사용자가 보낸 메시지인지 확인
     */
    public boolean isFromUser() {
        return type.isUserMessage();
    }

    /**
     * 반려동물이 보낸 메시지인지 확인
     */
    public boolean isFromPet() {
        return type.isPetMessage();
    }

    /**
     * 특정 채팅방의 메시지인지 확인
     */
    public boolean belongsToChatRoom(ChatRoomId chatRoomId) {
        return Objects.equals(this.chatRoomId, chatRoomId);
    }

    // getter
    public MessageId id() { return id; }
    public ChatRoomId chatRoomId() { return chatRoomId; }
    public MessageType type() { return type; }
    public String content() { return content; }
    public LocalDateTime createdAt() { return createdAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Message other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", chatRoomId=" + chatRoomId +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}