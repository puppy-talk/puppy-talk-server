package com.puppytalk.pet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 반려동물 단건 조회 쿼리 DTO
 */
@Schema(description = "반려동물 단건 조회 쿼리")
public record PetGetQuery(
    @Schema(description = "반려동물 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "반려동물 ID는 필수입니다")
    @Positive(message = "반려동물 ID는 양수여야 합니다")
    Long petId,
    
    @Schema(description = "반려동물 소유자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "소유자 ID는 필수입니다")
    @Positive(message = "소유자 ID는 양수여야 합니다")
    Long ownerId
) {

    public static PetGetQuery of(Long petId, Long ownerId) {
        return new PetGetQuery(petId, ownerId);
    }
}