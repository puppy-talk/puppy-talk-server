package com.puppy.talk.pet;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonaLookUpServiceImpl implements PersonaLookUpService {

    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public Persona findPersona(PersonaIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return personaRepository.findByIdentity(identity)
            .orElseThrow(() -> new PersonaNotFoundException(identity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Persona> findAllPersonas() {
        return personaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Persona> findActivePersonas() {
        return personaRepository.findByIsActive(true);
    }

    @Override
    @Transactional
    public Persona createPersona(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        return personaRepository.save(persona);
    }

    @Override
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
