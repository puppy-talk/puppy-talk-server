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
            notification.id() != null ? notification.id().getValue() : null,
            notification.userId().getValue(),
            notification.petId() != null ? notification.petId().getValue() : null,
            notification.chatRoomId() != null ? notification.chatRoomId().getValue() : null,
            notification.type().name(),
            notification.title(),
            notification.content(),
            notification.status().name(),
            notification.scheduledAt(),
            notification.sentAt(),
            notification.readAt(),
            notification.createdAt(),
            notification.retryCount(),
            notification.failureReason(),
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