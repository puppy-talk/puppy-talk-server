package com.puppy.talk.exception;

import com.puppy.talk.model.persona.PersonaIdentity;

public class PersonaNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersonaNotFoundException(PersonaIdentity personaIdentity) {
        super(personaIdentity != null ? "Persona not found with id: " + personaIdentity.id()
            : "Persona not found: invalid identity");
    }

    public PersonaNotFoundException(String message) {
        super(message);
    }
}