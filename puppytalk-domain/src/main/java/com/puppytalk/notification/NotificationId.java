package com.puppytalk.notification;

/**
 * 알림 ID 값 객체
 */
public record NotificationId(Long value) {
    
    public NotificationId {
        // 생성자 검증은 of() 메서드에서 수행
    }
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static NotificationId from(Long value) {
        return new NotificationId(value);
    }
    
    /**
     * 항상 새로운 인스턴스를 생성해 반환 (신규 생성용)
     */
    public static NotificationId create() {
        return new NotificationId(null);
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