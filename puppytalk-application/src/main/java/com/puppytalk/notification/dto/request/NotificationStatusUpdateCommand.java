package com.puppytalk.notification.dto.request;

/**
 * 알림 상태 업데이트 명령
 */
public record NotificationStatusUpdateCommand(
    Long notificationId,
    String status,      // SENT, READ, FAILED
    String failureReason // FAILED 상태일 때만 사용
) {
    
    public NotificationStatusUpdateCommand {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("NotificationId must be positive");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status must not be null or empty");
        }
    }
    
    /**
     * 기본 정적 팩토리 메서드
     */
    public static NotificationStatusUpdateCommand of(Long notificationId, String status, String failureReason) {
        return new NotificationStatusUpdateCommand(notificationId, status, failureReason);
    }
    
    public static NotificationStatusUpdateCommand sent(Long notificationId) {
        return new NotificationStatusUpdateCommand(notificationId, "SENT", null);
    }
    
    public static NotificationStatusUpdateCommand read(Long notificationId) {
        return new NotificationStatusUpdateCommand(notificationId, "READ", null);
    }
    
    public static NotificationStatusUpdateCommand failed(Long notificationId, String failureReason) {
        return new NotificationStatusUpdateCommand(notificationId, "FAILED", failureReason);
    }
}