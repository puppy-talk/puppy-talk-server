package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.Message;
import com.puppytalk.chat.MessageType;
import java.time.LocalDateTime;

/**
 * 메시지 조회 결과
 */
public record MessageResult(
    Long messageId,
    Long chatRoomId,
    MessageType type,
    String content,
    LocalDateTime createdAt
) {
    
    public static MessageResult from(Message message) {
        return new MessageResult(
            message.getId().value(),
            message.getChatRoomId().value(),
            message.getType(),
            message.getContent(),
            message.getCreatedAt()
        );
    }
}