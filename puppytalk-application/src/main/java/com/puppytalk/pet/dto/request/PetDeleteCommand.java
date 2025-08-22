package com.puppytalk.pet.dto.request;

import org.springframework.util.Assert;

public record PetDeleteCommand(
    Long petId,
    Long ownerId
) {
    public PetDeleteCommand {
        Assert.notNull(petId, "PetId cannot be null");
        Assert.notNull(ownerId, "OwnerId cannot be null");
    }
    
    public static PetDeleteCommand of(Long petId, Long ownerId) {
        return new PetDeleteCommand(petId, ownerId);
    }
}