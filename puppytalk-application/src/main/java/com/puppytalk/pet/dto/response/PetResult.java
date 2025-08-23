package com.puppytalk.pet.dto.response;

import com.puppytalk.pet.Pet;
import java.time.LocalDateTime;

public record PetResult(
    Long id,
    Long ownerId,
    String name,
    LocalDateTime createdAt,
    String status
) {
    
    public static PetResult from(Pet pet) {
        return new PetResult(
            pet.id().value(),
            pet.ownerId().value(),
            pet.name(),
            pet.createdAt(),
            pet.status().name()
        );
    }
}