package com.puppytalk.pet;

/**
 * 페르소나 식별자 값 객체
 */
public record PersonaId(Long value) {
    
    /**
     * PersonaId 생성 정적 팩토리 메서드
     */
    public static PersonaId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("페르소나 ID는 양수여야 합니다");
        }
        return new PersonaId(value);
    }
    
    /**
     * 신규 페르소나용 임시 ID 생성
     */
    public static PersonaId newPersona() {
        return new PersonaId(null);
    }
    
    /**
     * 저장된 페르소나인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }
}