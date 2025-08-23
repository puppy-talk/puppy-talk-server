package com.puppytalk.notification;

/**
 * 알림 ID 값 객체
 */
public record NotificationId(Long value) {
    
    public NotificationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("NotificationId must be positive");
        }
    }
    
    public static NotificationId of(Long value) {
        return new NotificationId(value);
    }
    
    /**
     * JPA 호환성을 위한 값 접근
     */
    public Long getValue() {
        return value;
    }
}