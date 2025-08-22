package com.puppytalk.pet;

/**
 * 페르소나 결과 DTO
 */
public record PersonaResult(
    Long id,
    String name,
    String description,
    boolean available
) {
    
    /**
     * Persona 도메인 객체로부터 결과 DTO 생성
     */
    public static PersonaResult from(Persona persona) {
        return new PersonaResult(
            persona.id().value(),
            persona.name(),
            persona.description(),
            persona.isAvailable()
        );
    }
}