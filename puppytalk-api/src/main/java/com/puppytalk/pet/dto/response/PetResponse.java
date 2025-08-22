package com.puppytalk.pet.dto.response;

import com.puppytalk.pet.PetResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 반려동물 응답 DTO
 */
@Schema(description = "반려동물 정보")
public record PetResponse(
    @Schema(description = "반려동물 ID", example = "1")
    Long id,
    
    @Schema(description = "반려동물 소유자 ID", example = "1")
    Long ownerId,
    
    @Schema(description = "반려동물 이름", example = "뽀삐")
    String name,
    
    @Schema(description = "페르소나 정보")
    PersonaResponse persona,
    
    @Schema(description = "생성일시", example = "2024-01-01T10:00:00")
    LocalDateTime createdAt,
    
    @Schema(description = "반려동물 상태", example = "ACTIVE")
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