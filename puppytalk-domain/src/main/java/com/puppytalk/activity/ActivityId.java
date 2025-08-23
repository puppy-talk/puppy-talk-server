package com.puppytalk.activity;

/**
 * 활동 ID 값 객체
 */
public record ActivityId(Long value) {
    
    public ActivityId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ActivityId must be positive");
        }
    }
    
    public static ActivityId of(Long value) {
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