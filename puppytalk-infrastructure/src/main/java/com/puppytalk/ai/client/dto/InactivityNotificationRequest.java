package com.puppytalk.ai.client.dto;

import java.util.List;

public record InactivityNotificationRequest(
    int userId,
    int petId,
    String petPersona,
    List<ChatMessage> lastMessages,
    int hoursSinceLastActivity,
    String timeOfDay
) {
    public InactivityNotificationRequest {
        if (userId <= 0) {
            throw new IllegalArgumentException("UserId must be positive");
        }
        if (petId <= 0) {
            throw new IllegalArgumentException("PetId must be positive");
        }
        if (petPersona == null || petPersona.trim().isEmpty()) {
            throw new IllegalArgumentException("PetPersona cannot be null or empty");
        }
        if (hoursSinceLastActivity < 2 || hoursSinceLastActivity > 72) {
            throw new IllegalArgumentException("HoursSinceLastActivity must be between 2 and 72");
        }
        if (timeOfDay != null && 
            !List.of("morning", "afternoon", "evening", "night").contains(timeOfDay)) {
            throw new IllegalArgumentException("TimeOfDay must be one of: morning, afternoon, evening, night");
        }
    }
}