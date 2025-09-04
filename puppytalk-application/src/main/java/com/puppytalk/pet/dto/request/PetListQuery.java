package com.puppytalk.pet.dto.request;

import jakarta.validation.constraints.NotNull;

public record PetListQuery(
    @NotNull(message = "소유자 ID는 필수입니다")
    Long ownerId
) {
    public static PetListQuery of(Long ownerId) {
        return new PetListQuery(ownerId);
    }
}