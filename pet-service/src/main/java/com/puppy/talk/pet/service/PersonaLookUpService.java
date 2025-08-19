package com.puppy.talk.pet.service;

import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaIdentity;
import java.util.List;

public interface PersonaLookUpService {

    Persona findPersona(PersonaIdentity identity);

    List<Persona> findAllPersonas();

    List<Persona> findActivePersonas();

    Persona createPersona(Persona persona);

    void deletePersona(PersonaIdentity identity);
}