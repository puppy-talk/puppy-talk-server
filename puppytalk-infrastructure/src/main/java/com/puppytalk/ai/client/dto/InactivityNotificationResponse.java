package com.puppytalk.ai.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InactivityNotificationResponse(
    boolean success,
    String notificationMessage,
    Integer generationTimeMs,
    LocalDateTime timestamp,
    LocalDateTime suggestedSendTime,
    String priority
) {
    public InactivityNotificationResponse {
        if (!success && (notificationMessage == null || notificationMessage.trim().isEmpty())) {
            throw new IllegalArgumentException("NotificationMessage is required for failed responses");
        }
        if (priority != null && 
            !List.of("low", "normal", "high").contains(priority)) {
            throw new IllegalArgumentException("Priority must be one of: low, normal, high");
        }
    }
}