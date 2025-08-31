package com.puppytalk.pet.dto.request;

import com.puppytalk.support.validation.Preconditions;

public record PetListQuery(
    Long ownerId
) {
    public PetListQuery {
        Preconditions.requireNonNull(ownerId, "OwnerId");
    }
    
    public static PetListQuery of(Long ownerId) {
        return new PetListQuery(ownerId);
    }
}