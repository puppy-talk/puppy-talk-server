package com.puppytalk.user;

public record UserId(Long value) {
    
    /**
     * Get the value (alias for compatibility)
     */
    public Long getValue() {
        return value;
    }
    
    public static UserId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("사용자 ID는 양수여야 합니다");
        }
        return new UserId(value);
    }
    
    public static UserId create() {
        return new UserId(null);
    }
    
    public static UserId from(Long value) {
        return new UserId(value);
    }

    public boolean isStored() {
        return value != null && value > 0;
    }
}