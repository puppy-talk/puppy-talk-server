package com.puppy.talk.pet;

import java.util.List;

public interface PersonaLookUpService {

    Persona findPersona(PersonaIdentity identity);

    List<Persona> findAllPersonas();

    List<Persona> findActivePersonas();

    Persona createPersona(Persona persona);

    void deletePersona(PersonaIdentity identity);
}