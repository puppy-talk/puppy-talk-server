package com.puppytalk.ai.client.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ChatResponse(
    boolean success,
    String messageId,
    String content,
    String model,
    Integer tokensUsed,
    Integer generationTimeMs,
    String conversationId,
    LocalDateTime timestamp,
    Map<String, Object> debugInfo
) {
    public ChatResponse {
        if (!success && (content == null || content.trim().isEmpty())) {
            throw new IllegalArgumentException("Content is required for failed responses");
        }
    }
}