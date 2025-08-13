package com.puppy.talk.pet;

import com.puppy.talk.user.UserIdentity;

public record Pet(
    PetIdentity identity,
    UserIdentity userId,
    PersonaIdentity personaId,
    String name,
    String breed,
    int age,
    String profileImageUrl
) {

    public Pet {
        // identity can be null for new pets before saving
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (personaId == null) {
            throw new IllegalArgumentException("PersonaId cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
}