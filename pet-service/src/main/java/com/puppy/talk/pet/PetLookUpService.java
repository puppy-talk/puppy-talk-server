package com.puppy.talk.pet;

import com.puppy.talk.user.UserIdentity;
import java.util.List;

public interface PetLookUpService {

    Pet findPet(PetIdentity identity);

    List<Pet> findAllPets();

    List<Pet> findPetsByUserId(UserIdentity userId);

    Pet createPet(Pet pet);

    void deletePet(PetIdentity identity);
}