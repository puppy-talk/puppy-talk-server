package com.puppytalk.user;

/**
 * 사용자 식별자 값 객체
 */
public record UserId(Long value) {
    
    /**
     * UserId 생성 정적 팩토리 메서드
     */
    public static UserId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("사용자 ID는 양수여야 합니다");
        }
        return new UserId(value);
    }
    
    /**
     * 신규 사용자용 임시 ID 생성
     */
    public static UserId newUser() {
        return new UserId(null);
    }
    
    /**
     * 저장된 사용자인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }
}