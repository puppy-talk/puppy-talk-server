package com.puppytalk.ai;

import com.puppytalk.chat.Message;
import java.util.List;

/**
 * AI 채팅 응답 생성에 필요한 컨텍스트 정보
 */
public record ChatContext(
    Long userId,
    Long petId,
    String petPersona,
    String userMessage,
    List<Message> conversationHistory
) {
    
    public ChatContext {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("PetId must be positive");
        }
        if (petPersona == null || petPersona.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet persona must not be null or empty");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message must not be null or empty");
        }
        if (conversationHistory == null) {
            throw new IllegalArgumentException("Conversation history must not be null");
        }
    }
}