package com.puppytalk.pet.dto.response;

/**
 * 페르소나 응답 DTO
 */
public record PersonaResponse(
    Long id,
    String name,
    String description,
    boolean available
) {
    
    /**
     * PersonaResult로부터 응답 DTO 생성
     */
    public static PersonaResponse from(PersonaResult personaResult) {
        return new PersonaResponse(
            personaResult.id(),
            personaResult.name(),
            personaResult.description(),
            personaResult.available()
        );
    }
}