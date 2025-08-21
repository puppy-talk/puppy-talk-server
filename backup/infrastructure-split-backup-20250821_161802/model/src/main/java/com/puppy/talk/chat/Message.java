package com.puppy.talk.chat;

import java.time.LocalDateTime;

public record Message(
    MessageIdentity identity,
    ChatRoomIdentity chatRoomId,
    SenderType senderType,
    String content,
    boolean isRead,
    LocalDateTime createdAt
) {

    public Message {
        // identity can be null for new messages before saving
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (senderType == null) {
            throw new IllegalArgumentException("SenderType cannot be null");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        // normalize
        content = content.trim();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Message(
        MessageIdentity identity,
        ChatRoomIdentity chatRoomId,
        SenderType senderType,
        String content,
        boolean isRead
    ) {
        this(identity, chatRoomId, senderType, content, isRead, LocalDateTime.now());
    }
    
    /**
     * 새로운 메시지를 생성합니다 (모든 필드 지정).
     */
    public static Message of(
        MessageIdentity identity,
        ChatRoomIdentity chatRoomId,
        SenderType senderType,
        String content,
        boolean isRead,
        LocalDateTime createdAt
    ) {
        return new Message(identity, chatRoomId, senderType, content, isRead, createdAt);
    }
    
    /**
     * 새로운 메시지를 생성합니다 (생성 시각 자동 설정).
     */
    public static Message of(
        MessageIdentity identity,
        ChatRoomIdentity chatRoomId,
        SenderType senderType,
        String content,
        boolean isRead
    ) {
        return new Message(identity, chatRoomId, senderType, content, isRead, LocalDateTime.now());
    }
}