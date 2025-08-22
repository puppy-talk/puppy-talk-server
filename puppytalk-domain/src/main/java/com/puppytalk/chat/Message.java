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
    
    /**
     * 사용자 메시지 생성 정적 팩토리 메서드
     */
    public static Message of(ChatRoomId chatRoomId, String content) {
        validateMessageCreation(chatRoomId, content);
        
        return new Message(
            MessageId.newMessage(),
            chatRoomId,
            MessageType.USER,
            content.trim(),
            LocalDateTime.now()
        );
    }
    
    /**
     * 반려동물 메시지 생성 정적 팩토리 메서드
     */
    public static Message createPetMessage(ChatRoomId chatRoomId, String content) {
        validateMessageCreation(chatRoomId, content);
        
        return new Message(
            MessageId.newMessage(),
            chatRoomId,
            MessageType.PET,
            content.trim(),
            LocalDateTime.now()
        );
    }
    
    /**
     * 기존 메시지 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static Message restore(MessageId id, ChatRoomId chatRoomId, MessageType type,
                                 String content, LocalDateTime createdAt) {
        validateRestore(id, chatRoomId, type, content, createdAt);
        
        return new Message(id, chatRoomId, type, content, createdAt);
    }
    
    private static void validateMessageCreation(ChatRoomId chatRoomId, String content) {
        if (chatRoomId == null || !chatRoomId.isStored()) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다");
        }
        if (content.trim().length() > 1000) {
            throw new IllegalArgumentException("메시지 내용은 1000자를 초과할 수 없습니다");
        }
    }
    
    private static void validateRestore(MessageId id, ChatRoomId chatRoomId, MessageType type,
                                      String content, LocalDateTime createdAt) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 메시지 ID가 필요합니다");
        }
        if (type == null) {
            throw new IllegalArgumentException("메시지 타입은 필수입니다");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        validateMessageCreation(chatRoomId, content);
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
    
    // Getters
    public MessageId getId() { return id; }
    public ChatRoomId getChatRoomId() { return chatRoomId; }
    public MessageType getType() { return type; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
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