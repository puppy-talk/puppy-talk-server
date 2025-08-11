package com.puppy.talk.service;

import com.puppy.talk.exception.PetNotFoundException;
import com.puppy.talk.infrastructure.PetRepository;
import com.puppy.talk.model.Pet;
import com.puppy.talk.model.PetIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetLookUpService {

    private final PetRepository petRepository;

    @Transactional(readOnly = true)
    public Pet findPet(PetIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return petRepository.findByIdentity(identity)
            .orElseThrow(() -> new PetNotFoundException(identity));
    }

    public List<Pet> findAllPets() {
        return petRepository.findAll();
    }
}