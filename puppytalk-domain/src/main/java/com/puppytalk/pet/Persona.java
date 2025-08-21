package com.puppytalk.pet;

/**
 * 반려동물 페르소나 값 객체
 * 
 * 페르소나는 반려동물의 성격, 특성, 대화 스타일을 정의하는 불변 객체입니다.
 * 한 번 설정된 페르소나는 수정할 수 없습니다.
 */
public record Persona(
    PersonaId id,
    String name,
    String description,
    String traits,
    String talkingStyle
) {
    
    /**
     * 페르소나 생성 정적 팩토리 메서드
     */
    public static Persona of(PersonaId id, String name, String description, String traits, String talkingStyle) {
        validatePersona(name, description, traits, talkingStyle);
        return new Persona(id, name, description, traits, talkingStyle);
    }
    
    private static void validatePersona(String name, String description, String traits, String talkingStyle) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("페르소나 이름은 필수입니다");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("페르소나 설명은 필수입니다");
        }
        if (traits == null || traits.trim().isEmpty()) {
            throw new IllegalArgumentException("페르소나 특성은 필수입니다");
        }
        if (talkingStyle == null || talkingStyle.trim().isEmpty()) {
            throw new IllegalArgumentException("페르소나 대화 스타일은 필수입니다");
        }
    }
    
    /**
     * 페르소나가 활성화 가능한지 확인
     */
    public boolean isAvailable() {
        return name != null && !name.trim().isEmpty();
    }
}