package com.puppy.talk.service;

import com.puppy.talk.exception.PersonaNotFoundException;
import com.puppy.talk.infrastructure.PersonaRepository;
import com.puppy.talk.model.persona.Persona;
import com.puppy.talk.model.persona.PersonaIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonaLookUpService {

    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public Persona findPersona(PersonaIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return personaRepository.findByIdentity(identity)
            .orElseThrow(() -> new PersonaNotFoundException(identity));
    }

    @Transactional(readOnly = true)
    public List<Persona> findAllPersonas() {
        return personaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Persona> findActivePersonas() {
        return personaRepository.findByIsActive(true);
    }

    @Transactional
    public Persona createPersona(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        return personaRepository.save(persona);
    }

    @Transactional
    public void deletePersona(PersonaIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (!personaRepository.findByIdentity(identity).isPresent()) {
            throw new PersonaNotFoundException(identity);
        }
        personaRepository.deleteByIdentity(identity);
    }
}