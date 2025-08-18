package com.puppy.talk.domain.chat.dto.response;

import com.puppy.talk.chat.Message;

import java.time.LocalDateTime;

public record MessageResponse(
    Long messageId,
    String content,
    String senderType,
    boolean isRead,
    LocalDateTime createdAt
) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
            message.identity().id(),
            message.content(),
            message.senderType().name(),
            message.isRead(),
            message.createdAt()
        );
    }
}
