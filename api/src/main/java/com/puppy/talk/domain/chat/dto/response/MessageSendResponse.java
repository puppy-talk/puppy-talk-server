package com.puppy.talk.domain.chat.dto.response;

import com.puppy.talk.dto.MessageSendResult;

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
