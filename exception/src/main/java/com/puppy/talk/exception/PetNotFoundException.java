package com.puppy.talk.exception;

import com.puppy.talk.model.PetIdentity;

public class PetNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PetNotFoundException(PetIdentity petIdentity) {
        super(petIdentity != null ? "Pet not found with id: " + petIdentity.getId()
            : "Pet not found: invalid identity");
    }

    public PetNotFoundException(String message) {
        super(message);
    }
}