package com.puppytalk.ai.client.dto;

public record ChatMessage(
    MessageRole role,
    String content,
    String timestamp
) {
    public ChatMessage {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (content == null || content.trim().isEmpty() || content.length() > 2000) {
            throw new IllegalArgumentException("Content must be non-empty and at most 2000 characters");
        }
    }
}