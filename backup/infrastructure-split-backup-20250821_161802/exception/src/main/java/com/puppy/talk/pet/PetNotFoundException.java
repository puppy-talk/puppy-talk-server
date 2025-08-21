package com.puppy.talk.pet;

public class PetNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PetNotFoundException(PetIdentity petIdentity) {
        super(petIdentity != null ? "Pet not found with id: " + petIdentity.id()
            : "Pet not found: invalid identity");
    }

    public PetNotFoundException(String message) {
        super(message);
    }
}