package com.puppy.talk.infrastructure;

import com.puppy.talk.model.Pet;
import com.puppy.talk.model.PetIdentity;
import java.util.List;
import java.util.Optional;

public interface PetRepository {

    Optional<Pet> findByIdentity(PetIdentity identity);

    List<Pet> findAll();

    Pet save(Pet pet);

    void deleteByIdentity(PetIdentity identity);
}