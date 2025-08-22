package com.puppytalk.chat.dto.request;

import org.springframework.util.Assert;

/**
 * 메시지 전송 커맨드
 */
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
    
    /**
     * 메시지 전송 커맨드 정적 팩토리 메서드
     */
    public static MessageSendCommand of(Long chatRoomId, Long userId, String content) {
        return new MessageSendCommand(chatRoomId, userId, content);
    }
}