package com.puppytalk.pet.dto.request;

import org.springframework.util.Assert;

public record PetGetQuery(
    Long petId,
    Long ownerId
) {
    public PetGetQuery {
        Assert.notNull(petId, "PetId cannot be null");
        Assert.notNull(ownerId, "OwnerId cannot be null");
    }
    
    public static PetGetQuery of(Long petId, Long ownerId) {
        return new PetGetQuery(petId, ownerId);
    }
}