package com.puppytalk.pet;

/**
 * 반려동물 식별자 값 객체
 */
public record PetId(Long value) {
    
    /**
     * PetId 생성 정적 팩토리 메서드
     */
    public static PetId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("반려동물 ID는 양수여야 합니다");
        }
        return new PetId(value);
    }
    
    /**
     * 신규 반려동물용 임시 ID 생성
     */
    public static PetId newPet() {
        return new PetId(null);
    }
    
    /**
     * 저장된 반려동물인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }
}