package com.puppy.talk.ai.provider.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 제공업체로부터 받은 응답 정보
 */
public record AiResponse(
    String content,
    String model,
    String providerId,
    Integer tokensUsed,
    LocalDateTime generatedAt,
    Map<String, Object> metadata
) {
    public AiResponse {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (providerId == null || providerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider ID cannot be null or empty");
        }
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    /**
     * 기본 응답을 생성합니다.
     */
    public static AiResponse of(String content, String model, String providerId) {
        return new AiResponse(content, model, providerId, null, LocalDateTime.now(), Map.of());
    }

    /**
     * 토큰 사용량과 함께 응답을 생성합니다.
     */
    public static AiResponse of(String content, String model, String providerId, Integer tokensUsed) {
        return new AiResponse(content, model, providerId, tokensUsed, LocalDateTime.now(), Map.of());
    }

    /**
     * 메타데이터와 함께 응답을 생성합니다.
     */
    public static AiResponse of(String content, String model, String providerId, Integer tokensUsed, Map<String, Object> metadata) {
        return new AiResponse(content, model, providerId, tokensUsed, LocalDateTime.now(), metadata != null ? metadata : Map.of());
    }
}
