package com.puppytalk.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 새 메시지 폴링 응답
 */
public record NewMessageListResponse(
    List<MessageResponse> messages,
    boolean hasNewMessages,
    LocalDateTime polledAt
) {
    public static NewMessageListResponse from(NewMessageResult result) {
        List<MessageResponse> messages = result.messages().stream()
            .map(MessageResponse::from)
            .toList();
        
        return new NewMessageListResponse(
            messages,
            result.hasNewMessages(),
            result.polledAt()
        );
    }
}