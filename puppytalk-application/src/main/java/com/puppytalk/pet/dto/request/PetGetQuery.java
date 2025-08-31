package com.puppytalk.pet.dto.request;

import com.puppytalk.support.validation.Preconditions;

public record PetGetQuery(
    Long petId,
    Long ownerId
) {
    public PetGetQuery {
        Preconditions.requireNonNull(petId, "PetId");
        Preconditions.requireNonNull(ownerId, "OwnerId");
    }
    
    public static PetGetQuery of(Long petId, Long ownerId) {
        return new PetGetQuery(petId, ownerId);
    }
}