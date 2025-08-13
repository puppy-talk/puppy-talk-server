package com.puppy.talk.service.pet;

import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.user.UserIdentity;
import java.util.List;

public interface PetLookUpService {

    Pet findPet(PetIdentity identity);

    List<Pet> findAllPets();

    List<Pet> findPetsByUserId(UserIdentity userId);

    Pet createPet(Pet pet);

    void deletePet(PetIdentity identity);
}