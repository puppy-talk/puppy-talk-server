package com.puppy.talk.controller;

import com.puppy.talk.service.dto.MessageSendResult;

import java.time.LocalDateTime;

public record MessageSendResponse(
    Long messageId,
    String content,
    String senderType,
    LocalDateTime sentAt,
    Long chatRoomId,
    String chatRoomName,
    LocalDateTime chatRoomLastMessageAt
) {

    public static MessageSendResponse from(MessageSendResult result) {
        return new MessageSendResponse(
            result.message().identity().id(),
            result.message().content(),
            result.message().senderType().name(),
            result.message().createdAt(),
            result.chatRoom().identity().id(),
            result.chatRoom().roomName(),
            result.chatRoom().lastMessageAt()
        );
    }
}
