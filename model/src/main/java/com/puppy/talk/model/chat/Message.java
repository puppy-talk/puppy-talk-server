package com.puppy.talk.model.chat;

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
}