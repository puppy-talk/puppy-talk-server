package com.puppytalk.notification.dto.response;

import com.puppytalk.notification.Notification;

import java.time.LocalDateTime;

/**
 * 알림 조회/생성 결과
 */
public record NotificationResult(
    Long notificationId,
    Long userId,
    Long petId,
    Long chatRoomId,
    String type,
    String title,
    String content,
    String status,
    LocalDateTime scheduledAt,
    LocalDateTime sentAt,
    LocalDateTime readAt,
    LocalDateTime createdAt,
    int retryCount,
    String failureReason,
    boolean found
) {
    
    public static NotificationResult from(Notification notification) {
        return new NotificationResult(
            notification.getId() != null ? notification.getId().getValue() : null,
            notification.getUserId().getValue(),
            notification.getPetId() != null ? notification.getPetId().getValue() : null,
            notification.getChatRoomId() != null ? notification.getChatRoomId().getValue() : null,
            notification.getType().name(),
            notification.getTitle(),
            notification.getContent(),
            notification.getStatus().name(),
            notification.getScheduledAt(),
            notification.getSentAt(),
            notification.getReadAt(),
            notification.getCreatedAt(),
            0, // retryCount - not available in domain
            null, // failureReason - not available in domain  
            true
        );
    }
    
    public static NotificationResult created(Long notificationId) {
        return new NotificationResult(
            notificationId,
            null,
            null,
            null,
            null,
            null,
            null,
            "CREATED",
            null,
            null,
            null,
            LocalDateTime.now(),
            0,
            null,
            true
        );
    }
    
    public static NotificationResult notFound() {
        return new NotificationResult(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            null,
            false
        );
    }
}