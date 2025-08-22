package com.puppytalk.pet.dto.response;

import com.puppytalk.pet.PetResult;
import java.time.LocalDateTime;

/**
 * 반려동물 응답 DTO
 */
public record PetResponse(
    Long id,
    Long ownerId,
    String name,
    PersonaResponse persona,
    LocalDateTime createdAt,
    String status
) {
    
    /**
     * PetResult로부터 응답 DTO 생성
     */
    public static PetResponse from(PetResult petResult) {
        return new PetResponse(
            petResult.id(),
            petResult.ownerId(),
            petResult.name(),
            PersonaResponse.from(petResult.persona()),
            petResult.createdAt(),
            petResult.status()
        );
    }
}