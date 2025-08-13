package com.puppy.talk.service.pet;

import com.puppy.talk.model.pet.Persona;
import com.puppy.talk.model.pet.PersonaIdentity;
import java.util.List;

public interface PersonaLookUpService {

    Persona findPersona(PersonaIdentity identity);

    List<Persona> findAllPersonas();

    List<Persona> findActivePersonas();

    Persona createPersona(Persona persona);

    void deletePersona(PersonaIdentity identity);
}