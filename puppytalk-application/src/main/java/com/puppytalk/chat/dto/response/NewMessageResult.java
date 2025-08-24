package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.Message;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 새 메시지 폴링 결과
 */
public record NewMessageResult(
    List<MessageResult> messages,
    boolean hasNewMessages,
    LocalDateTime polledAt
) {
    public static NewMessageResult from(List<Message> messages) {
        List<MessageResult> messageResults = messages.stream()
            .map(MessageResult::from)
            .toList();
        
        return new NewMessageResult(
            messageResults,
            !messages.isEmpty(),
            LocalDateTime.now()
        );
    }
}