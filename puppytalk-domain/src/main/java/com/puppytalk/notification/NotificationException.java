package com.puppytalk.notification;

import com.puppytalk.user.UserId;

/**
 * 알림 관련 예외
 * 
 * Backend 관점: 알림 시스템 안정성을 위한 명확한 예외 분류
 */
public class NotificationException extends RuntimeException {
    
    private final UserId userId;
    private final NotificationId notificationId;
    private final NotificationType notificationType;
    
    public NotificationException(String message) {
        super(message);
        this.userId = null;
        this.notificationId = null;
        this.notificationType = null;
    }
    
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.notificationId = null;
        this.notificationType = null;
    }
    
    public NotificationException(String message, UserId userId, NotificationType notificationType) {
        super(message);
        this.userId = userId;
        this.notificationId = null;
        this.notificationType = notificationType;
    }
    
    public NotificationException(String message, NotificationId notificationId, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.notificationId = notificationId;
        this.notificationType = null;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public NotificationId getNotificationId() {
        return notificationId;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    /**
     * 알림 생성 실패
     */
    public static NotificationException creationFailed(UserId userId, NotificationType type, String reason) {
        return new NotificationException(
            String.format("Notification creation failed for user %s with type %s: %s", 
                userId != null ? userId.getValue() : "unknown",
                type != null ? type.name() : "unknown", 
                reason),
            userId, 
            type
        );
    }
    
    /**
     * 알림 발송 실패
     */
    public static NotificationException sendingFailed(NotificationId notificationId, String reason) {
        return new NotificationException(
            String.format("Notification sending failed for notification %s: %s", 
                notificationId != null ? notificationId.getValue() : "unknown", 
                reason),
            notificationId,
            null
        );
    }
    
    /**
     * 알림 발송 실패 (원인 포함)
     */
    public static NotificationException sendingFailed(NotificationId notificationId, String reason, Throwable cause) {
        return new NotificationException(
            String.format("Notification sending failed for notification %s: %s", 
                notificationId != null ? notificationId.getValue() : "unknown", 
                reason),
            notificationId,
            cause
        );
    }
    
    /**
     * 알림을 찾을 수 없음
     */
    public static NotificationException notificationNotFound(NotificationId notificationId) {
        return new NotificationException(
            String.format("Notification not found: %s", 
                notificationId != null ? notificationId.getValue() : "unknown")
        );
    }
    
    /**
     * 일일 알림 발송 제한 초과
     */
    public static NotificationException dailyLimitExceeded(UserId userId, int limit) {
        return new NotificationException(
            String.format("Daily notification limit exceeded for user %s: %d", 
                userId != null ? userId.getValue() : "unknown", 
                limit),
            userId, 
            null
        );
    }
    
    /**
     * AI 메시지 생성 실패
     */
    public static NotificationException aiGenerationFailed(UserId userId, String reason) {
        return new NotificationException(
            String.format("AI message generation failed for user %s: %s", 
                userId != null ? userId.getValue() : "unknown", 
                reason),
            userId, 
            NotificationType.INACTIVITY_MESSAGE
        );
    }
    
    /**
     * 알림 스케줄링 실패
     */
    public static NotificationException schedulingFailed(String reason) {
        return new NotificationException("Notification scheduling failed: " + reason);
    }
    
    /**
     * 배치 처리 실패
     */
    public static NotificationException batchProcessingFailed(String reason, Throwable cause) {
        return new NotificationException("Notification batch processing failed: " + reason, cause);
    }
    
    /**
     * AI 메시지 생성 실패 (단순)
     */
    public static class AiGenerationFailed extends NotificationException {
        public AiGenerationFailed(String message) {
            super(message);
        }
    }
    
    /**
     * 알림 생성 실패 (원인 포함)
     */
    public static class CreationFailed extends NotificationException {
        public CreationFailed(String message, UserId userId, Throwable cause) {
            super(message, cause);
        }
    }
}