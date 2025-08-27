package com.puppytalk.pet;

import com.puppytalk.user.UserId;
import java.util.List;


public class PetDomainService {
    
    private final PetRepository petRepository;
    
    public PetDomainService(PetRepository petRepository) {
        if (petRepository == null) {
            throw new IllegalArgumentException("PetRepository must not be null");
        }
        this.petRepository = petRepository;
    }

    public Pet findPet(PetId petId, UserId ownerId) {
        if (petId == null) {
            throw new IllegalArgumentException("PetId must not be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("OwnerId must not be null");
        }

        return petRepository.findByIdAndOwnerId(petId, ownerId)
            .orElseThrow(() -> new PetNotFoundException(petId));
    }
    
    public void createPet(UserId ownerId, String petName, String persona) {
        if (ownerId == null) {
            throw new IllegalArgumentException("OwnerId must not be null");
        }

        if (petName == null || petName.isBlank()) {
            throw new IllegalArgumentException("PetName cannot be null or empty");
        }

        if (persona == null || persona.isBlank()) {
            throw new IllegalArgumentException("Persona cannot be null or empty");
        }

        Pet pet = Pet.create(ownerId, petName, persona);
        petRepository.create(pet);
    }

    public List<Pet> findPetList(UserId ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("OwnerId must not be null");
        }
        return petRepository.findByOwnerId(ownerId);
    }
    
    public void deletePet(PetId petId, UserId ownerId) {
        Pet pet = findPet(petId, ownerId);
        Pet deletedPet = pet.withDeletedStatus();
        petRepository.delete(deletedPet);
    }
}