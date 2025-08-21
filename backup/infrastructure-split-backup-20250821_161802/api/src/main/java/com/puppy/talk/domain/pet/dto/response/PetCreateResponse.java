package com.puppy.talk.domain.pet.dto.response;

import com.puppy.talk.pet.Pet;

public record PetCreateResponse(
    Long id,
    Long userId,
    Long personaId,
    String name,
    String breed,
    Integer age,
    String profileImageUrl,
    Long chatRoomId
) {

    public static PetCreateResponse of(Pet pet, Long chatRoomId) {
        return new PetCreateResponse(
            pet.identity().id(),
            pet.userId().id(),
            pet.personaId().id(),
            pet.name(),
            pet.breed(),
            pet.age(),
            pet.profileImageUrl(),
            chatRoomId
        );
    }
}