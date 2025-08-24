package com.puppytalk.notification;

/**
 * 알림 ID 값 객체
 */
public record NotificationId(Long value) {
    
    public NotificationId {
        // 생성자 검증은 of() 메서드에서 수행
    }
    
    public static NotificationId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("NotificationId must be positive");
        }
        return new NotificationId(value);
    }
    
    public static NotificationId create() {
        return new NotificationId(null);
    }
    
    public static NotificationId from(Long value) {
        return new NotificationId(value);
    }
    
    /**
     * ID가 유효한지 확인
     */
    public boolean isValid() {
        return value != null && value > 0;
    }
    
    /**
     * JPA 호환성을 위한 값 접근
     */
    public Long getValue() {
        return value;
    }
}