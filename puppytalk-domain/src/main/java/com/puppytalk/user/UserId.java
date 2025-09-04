package com.puppytalk.user;

/**
 * 사용자 ID를 나타내는 값 객체
 */
public record UserId(Long value) {
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static UserId from(Long value) {
        return new UserId(value);
    }
}