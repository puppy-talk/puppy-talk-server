package com.puppytalk.chat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 메시지 엔티티
 * 
 * 채팅방 내에서 사용자와 반려동물 간에 주고받는 개별 메시지입니다.
 */
public class Message {
    
    private final MessageId id;
    private final ChatRoomId chatRoomId;  // 채팅방 ID
    private final MessageType type;       // 메시지 타입 (USER/PET)
    private final String content;         // 메시지 내용
    private final LocalDateTime sentAt;   // 전송 시각
    
    private Message(MessageId id, ChatRoomId chatRoomId, MessageType type, 
                   String content, LocalDateTime sentAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.type = type;
        this.content = content;
        this.sentAt = sentAt;
    }
    
    /**
     * 새로운 사용자 메시지 생성 정적 팩토리 메서드
     */
    public static Message createUserMessage(ChatRoomId chatRoomId, String content) {
        validateMessage(chatRoomId, content);
        
        return new Message(
            MessageId.newMessage(),
            chatRoomId,
            MessageType.USER,
            content.trim(),
            LocalDateTime.now()
        );
    }
    
    /**
     * 새로운 반려동물 메시지 생성 정적 팩토리 메서드
     */
    public static Message createPetMessage(ChatRoomId chatRoomId, String content) {
        validateMessage(chatRoomId, content);
        
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
                                 String content, LocalDateTime sentAt) {
        validateRestore(id, chatRoomId, type, content, sentAt);
        
        return new Message(id, chatRoomId, type, content, sentAt);
    }
    
    private static void validateMessage(ChatRoomId chatRoomId, String content) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("채팅방 ID는 필수입니다");
        }
        if (!chatRoomId.isStored()) {
            throw new IllegalArgumentException("저장된 채팅방만 메시지를 생성할 수 있습니다");
        }
        validateContent(content);
    }
    
    private static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다");
        }
        if (content.trim().length() > 1000) {
            throw new IllegalArgumentException("메시지는 1000자를 초과할 수 없습니다");
        }
    }
    
    private static void validateRestore(MessageId id, ChatRoomId chatRoomId, MessageType type,
                                       String content, LocalDateTime sentAt) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 메시지 ID가 필요합니다");
        }
        if (type == null) {
            throw new IllegalArgumentException("메시지 타입은 필수입니다");
        }
        if (sentAt == null) {
            throw new IllegalArgumentException("전송 시각은 필수입니다");
        }
        validateMessage(chatRoomId, content);
    }
    
    /**
     * 사용자 메시지인지 확인
     */
    public boolean isUserMessage() {
        return type.isUserMessage();
    }
    
    /**
     * 반려동물 메시지인지 확인
     */
    public boolean isPetMessage() {
        return type.isPetMessage();
    }
    
    /**
     * 특정 채팅방의 메시지인지 확인
     */
    public boolean belongsToChatRoom(ChatRoomId chatRoomId) {
        return Objects.equals(this.chatRoomId, chatRoomId);
    }
    
    /**
     * 메시지 전송 후 경과 시간 확인 (분 단위)
     */
    public long getMinutesSinceSent() {
        return java.time.Duration.between(sentAt, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * 메시지 미리보기 (최대 50자)
     */
    public String getPreview() {
        if (content.length() <= 50) {
            return content;
        }
        return content.substring(0, 47) + "...";
    }
    
    // Getters
    public MessageId getId() { return id; }
    public ChatRoomId getChatRoomId() { return chatRoomId; }
    public MessageType getType() { return type; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    
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
                ", preview='" + getPreview() + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}