package com.puppy.talk.service;

import com.puppy.talk.exception.pet.PetNotFoundException;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.user.UserIdentity;
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

    @Transactional(readOnly = true)
    public List<Pet> findAllPets() {
        return petRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pet> findPetsByUserId(UserIdentity userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        return petRepository.findByUserId(userId);
    }

    @Transactional
    public Pet createPet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        return petRepository.save(pet);
    }

    @Transactional
    public void deletePet(PetIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        if (!petRepository.findByIdentity(identity).isPresent()) {
            throw new PetNotFoundException(identity);
        }
        petRepository.deleteByIdentity(identity);
    }
}