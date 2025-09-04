package com.puppytalk.pet;

/**
 * 반려동물 ID를 나타내는 값 객체
 */
public record PetId(Long value) {
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static PetId from(Long value) {
        return new PetId(value);
    }
}