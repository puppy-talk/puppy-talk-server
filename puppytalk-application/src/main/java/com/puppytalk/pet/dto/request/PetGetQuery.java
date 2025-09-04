package com.puppytalk.pet.dto.request;

import jakarta.validation.constraints.NotNull;

public record PetGetQuery(
    @NotNull(message = "반려동물 ID는 필수입니다")
    Long petId,
    
    @NotNull(message = "소유자 ID는 필수입니다")
    Long ownerId
) {
    public static PetGetQuery of(Long petId, Long ownerId) {
        return new PetGetQuery(petId, ownerId);
    }
}