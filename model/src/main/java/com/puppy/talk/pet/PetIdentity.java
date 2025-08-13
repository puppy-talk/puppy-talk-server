package com.puppy.talk.pet;

public record PetIdentity(Long id) {

    public PetIdentity {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public static PetIdentity of(Long id) {
        return new PetIdentity(id);
    }
}