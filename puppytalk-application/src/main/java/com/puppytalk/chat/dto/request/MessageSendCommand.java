package com.puppytalk.chat.dto.request;

import org.springframework.util.Assert;

public record MessageSendCommand(
    Long chatRoomId,
    Long userId,
    String content
) {
    public MessageSendCommand {
        Assert.notNull(chatRoomId, "ChatRoomId cannot be null");
        Assert.notNull(userId, "UserId cannot be null");
        Assert.hasText(content, "Content must not be null or empty");
    }
    
    public static MessageSendCommand of(Long chatRoomId, Long userId, String content) {
        return new MessageSendCommand(chatRoomId, userId, content);
    }
}