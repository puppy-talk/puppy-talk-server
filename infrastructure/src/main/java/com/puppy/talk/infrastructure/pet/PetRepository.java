package com.puppy.talk.infrastructure.pet;

import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.PetIdentity;
import com.puppy.talk.model.user.UserIdentity;
import java.util.List;
import java.util.Optional;

public interface PetRepository {

    Optional<Pet> findByIdentity(PetIdentity identity);

    List<Pet> findAll();

    List<Pet> findByUserId(UserIdentity userId);

    Pet save(Pet pet);

    void deleteByIdentity(PetIdentity identity);
}