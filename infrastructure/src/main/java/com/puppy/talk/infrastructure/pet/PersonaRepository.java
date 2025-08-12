package com.puppy.talk.infrastructure.pet;

import com.puppy.talk.model.pet.Persona;
import com.puppy.talk.model.pet.PersonaIdentity;
import java.util.List;
import java.util.Optional;

public interface PersonaRepository {

    Optional<Persona> findByIdentity(PersonaIdentity identity);

    List<Persona> findAll();

    List<Persona> findByIsActive(boolean isActive);

    Persona save(Persona persona);

    void deleteByIdentity(PersonaIdentity identity);
}