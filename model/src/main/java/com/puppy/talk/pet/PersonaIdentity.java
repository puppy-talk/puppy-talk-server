package com.puppy.talk.pet;

public record PersonaIdentity(Long id) {

    public PersonaIdentity {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
    }

    public static PersonaIdentity of(Long id) {
        return new PersonaIdentity(id);
    }
}