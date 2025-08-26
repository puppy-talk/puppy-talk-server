package com.puppytalk.user;

public record UserId(Long value) {
    
    /**
     * Get the value (alias for compatibility)
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static UserId from(Long value) {
        return new UserId(value);
    }
    
    /**
     * 항상 새로운 인스턴스를 생성해 반환 (신규 생성용)
     */
    public static UserId create() {
        return new UserId(null);
    }

    public boolean isStored() {
        return value != null && value > 0;
    }
}