package com.puppy.talk.model.pet;

import com.puppy.talk.model.user.UserIdentity;

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
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
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