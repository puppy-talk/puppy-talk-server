package com.puppytalk.pet;

import java.time.LocalDateTime;

/**
 * 반려동물 결과 DTO
 */
public record PetResult(
    Long id,
    Long ownerId,
    String name,
    PersonaResult persona,
    LocalDateTime createdAt,
    String status
) {
    
    /**
     * Pet 도메인 객체로부터 결과 DTO 생성
     */
    public static PetResult from(Pet pet) {
        return new PetResult(
            pet.getId().value(),
            pet.getOwnerId(),
            pet.getName(),
            PersonaResult.from(pet.getPersona()),
            pet.getCreatedAt(),
            pet.getStatus().name()
        );
    }
}