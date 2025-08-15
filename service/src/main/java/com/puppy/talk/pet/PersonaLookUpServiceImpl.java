package com.puppy.talk.pet;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonaLookUpServiceImpl implements PersonaLookUpService {

    private final PersonaRepository personaRepository;
    
    // TODO: Consider adding caching for frequently accessed personas
    // @Cacheable("personas") could improve performance for read operations

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
        
        // TODO: Add validation for persona business rules
        // e.g., duplicate name checks, persona template validation
        
        return personaRepository.save(persona);
    }

    @Override
    @Transactional
    public void deletePersona(PersonaIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        
        personaRepository.findByIdentity(identity)
            .ifPresentOrElse(
                persona -> personaRepository.deleteByIdentity(identity),
                () -> { throw new PersonaNotFoundException(identity); }
            );
    }
}
