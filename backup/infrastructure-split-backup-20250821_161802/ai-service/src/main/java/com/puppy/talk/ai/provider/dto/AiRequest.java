package com.puppy.talk.ai.provider.dto;

import java.util.Map;

/**
 * AI 제공업체에 전달할 요청 정보
 */
public record AiRequest(
    String prompt,
    String model,
    Integer maxTokens,
    Double temperature,
    Map<String, Object> additionalParameters
) {
    public AiRequest {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be null or empty");
        }
        if (maxTokens != null && maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
        if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
            throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
        }
    }

    /**
     * 기본 파라미터로 요청을 생성합니다.
     */
    public static AiRequest of(String prompt, String model) {
        return new AiRequest(prompt, model, 150, 0.8, Map.of());
    }

    /**
     * 파라미터를 지정하여 요청을 생성합니다.
     */
    public static AiRequest of(String prompt, String model, Integer maxTokens, Double temperature) {
        return new AiRequest(prompt, model, maxTokens, temperature, Map.of());
    }

    /**
     * 추가 파라미터와 함께 요청을 생성합니다.
     */
    public static AiRequest of(String prompt, String model, Integer maxTokens, Double temperature, Map<String, Object> additionalParameters) {
        return new AiRequest(prompt, model, maxTokens, temperature, additionalParameters != null ? additionalParameters : Map.of());
    }
}
