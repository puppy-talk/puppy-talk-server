package com.puppytalk.pet.dto.request;

import org.springframework.util.Assert;

public record PetListQuery(
    Long ownerId
) {
    public PetListQuery {
        Assert.notNull(ownerId, "OwnerId cannot be null");
    }
    
    public static PetListQuery of(Long ownerId) {
        return new PetListQuery(ownerId);
    }
}