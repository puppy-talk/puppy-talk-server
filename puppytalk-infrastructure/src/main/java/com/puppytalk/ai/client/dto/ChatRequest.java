package com.puppytalk.ai.client.dto;

import java.util.List;
import org.springframework.util.Assert;

public record ChatRequest(
    int userId,
    int petId,
    String userMessage,
    String petPersona,
    List<ChatMessage> conversationHistory
) {
    public ChatRequest {
        Assert.isTrue(userId > 0, "UserId must be positive");
        Assert.isTrue(petId > 0, "PetId must be positive");
        Assert.hasText(userMessage, "UserMessage cannot be null or empty");
        Assert.hasText(petPersona, "PetPersona cannot be null or empty");
    }
}