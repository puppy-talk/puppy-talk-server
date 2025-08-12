package com.puppy.talk.infrastructure;

import com.puppy.talk.model.persona.Persona;
import com.puppy.talk.model.persona.PersonaIdentity;
import java.util.List;
import java.util.Optional;

public interface PersonaRepository {

    Optional<Persona> findByIdentity(PersonaIdentity identity);

    List<Persona> findAll();

    List<Persona> findByIsActive(boolean isActive);

    Persona save(Persona persona);

    void deleteByIdentity(PersonaIdentity identity);
}