package com.puppytalk.pet.dto.request;

import org.springframework.util.Assert;

public record PetCreateCommand(
    Long ownerId,
    String petName
) {
    public PetCreateCommand {
        Assert.notNull(ownerId, "OwnerId cannot be null");
        Assert.hasText(petName, "PetName must not be null or empty");
    }
    
    public static PetCreateCommand of(Long ownerId, String petName) {
        return new PetCreateCommand(ownerId, petName);
    }
}