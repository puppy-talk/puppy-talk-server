package com.puppytalk.ai.client.dto;

import java.util.List;
import java.util.Map;

public record ChatRequest(
    int userId,
    int petId,
    int chatRoomId,
    String userMessage,
    PetPersona petPersona,
    List<ChatMessage> conversationHistory,
    Map<String, Object> context,
    Integer maxTokens,
    Double temperature
) {
    public ChatRequest {
        if (userId <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
        if (petId <= 0) {
            throw new IllegalArgumentException("PetId must be positive");
        }
        if (chatRoomId <= 0) {
            throw new IllegalArgumentException("ChatRoomId must be positive");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("UserMessage cannot be null or empty");
        }
        if (petPersona == null) {
            throw new IllegalArgumentException("PetPersona cannot be null");
        }
        if (maxTokens != null && maxTokens < 10) {
            throw new IllegalArgumentException("MaxTokens must be at least 10");
        }
        if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
            throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
        }
    }
}