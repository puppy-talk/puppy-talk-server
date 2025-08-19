package com.puppy.talk.pet.dto;

import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.pet.PersonaIdentity;

public record PetCreateCommand(
    UserIdentity userId,
    PersonaIdentity personaId,
    String name,
    String breed,
    Integer age,
    String profileImageUrl
) {
    public static PetCreateCommand of(
        Long userId,
        Long personaId,
        String name,
        String breed,
        Integer age,
        String profileImageUrl
    ) {
        return new PetCreateCommand(
            UserIdentity.of(userId),
            PersonaIdentity.of(personaId),
            name,
            breed,
            age,
            profileImageUrl
        );
    }
}
