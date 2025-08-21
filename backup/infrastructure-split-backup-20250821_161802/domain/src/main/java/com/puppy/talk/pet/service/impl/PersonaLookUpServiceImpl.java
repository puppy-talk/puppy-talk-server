package com.puppy.talk.pet.service.impl;

import com.puppy.talk.pet.service.PersonaLookUpService;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaIdentity;
import com.puppy.talk.pet.PersonaNotFoundException;
import com.puppy.talk.pet.PersonaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 페르소나 도메인 서비스 구현체
 * 
 * 페르소나 관리와 관련된 비즈니스 로직을 캡슐화하고
 * 도메인 규칙을 적용합니다.
 */
@Service
@RequiredArgsConstructor
public class PersonaLookUpServiceImpl implements PersonaLookUpService {

    private final PersonaRepository personaRepository;
    
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