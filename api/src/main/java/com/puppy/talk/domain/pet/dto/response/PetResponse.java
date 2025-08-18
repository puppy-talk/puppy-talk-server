package com.puppy.talk.pet.dto.response;

import com.puppy.talk.pet.Pet;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PetResponse {

    private final Long id;
    private final String name;
    private final String breed;
    private final int age;

    public static PetResponse from(Pet pet) {
        return PetResponse.builder()
            .id(pet.identity().id())
            .name(pet.name())
            .breed(pet.breed())
            .age(pet.age())
            .build();
    }
}