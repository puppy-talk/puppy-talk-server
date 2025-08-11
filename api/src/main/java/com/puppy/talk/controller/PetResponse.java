package com.puppy.talk.controller;

import com.puppy.talk.model.Pet;
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
            .id(pet.getIdentity().getId())
            .name(pet.getName())
            .breed(pet.getBreed())
            .age(pet.getAge())
            .build();
    }
}