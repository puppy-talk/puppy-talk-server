package com.puppytalk.pet;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.UserId;
import java.util.List;


public class PetDomainService {
    
    private final PetRepository petRepository;
    
    public PetDomainService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public Pet getPet(PetId petId, UserId ownerId) {
        Preconditions.requireValidId(petId, "PetId");
        Preconditions.requireValidId(ownerId, "OwnerId");

        return petRepository.findByIdAndOwnerId(petId, ownerId)
            .orElseThrow(() -> new PetNotFoundException(petId));
    }
    
    public void createPet(UserId ownerId, String petName, String persona) {
        Preconditions.requireValidId(ownerId, "OwnerId");
        Preconditions.requireNonBlank(petName, "PetName");
        Preconditions.requireNonBlank(persona, "Persona");

        Pet pet = Pet.create(ownerId, petName, persona);
        petRepository.create(pet);
    }

    public List<Pet> findPetList(UserId ownerId) {
        Preconditions.requireValidId(ownerId, "OwnerId");
        return petRepository.findByOwnerId(ownerId);
    }
    
    public void deletePet(PetId petId, UserId ownerId) {
        Pet pet = getPet(petId, ownerId);
        Pet deletedPet = pet.withDeletedStatus();
        petRepository.delete(deletedPet);
    }
}