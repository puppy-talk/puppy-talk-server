package com.puppytalk.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 새 메시지 폴링 응답
 */
public record NewMessagesResponse(
    List<MessageResponse> messages,
    boolean hasNewMessages,
    LocalDateTime polledAt
) {
    public static NewMessagesResponse from(NewMessageResult result) {
        List<MessageResponse> messages = result.messages().stream()
            .map(MessageResponse::from)
            .toList();
        
        return new NewMessagesResponse(
            messages,
            result.hasNewMessages(),
            result.polledAt()
        );
    }
}