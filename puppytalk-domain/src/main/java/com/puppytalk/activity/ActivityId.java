package com.puppytalk.activity;

/**
 * 활동 ID 값 객체
 */
public record ActivityId(Long value) {
    
    public ActivityId {
        // 생성자 검증은 of() 메서드에서 수행
    }
    
    public static ActivityId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ActivityId must be positive");
        }
        return new ActivityId(value);
    }
    
    public static ActivityId create() {
        return new ActivityId(null);
    }
    
    public static ActivityId from(Long value) {
        return new ActivityId(value);
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