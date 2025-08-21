package com.puppy.talk.pet.service.impl;

import com.puppy.talk.pet.service.PetLookUpService;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.PetNotFoundException;
import com.puppy.talk.pet.PetRepository;
import com.puppy.talk.user.UserIdentity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 펫 도메인 서비스 구현체
 * 
 * 펫 관리와 관련된 비즈니스 로직을 캡슐화하고
 * 도메인 규칙을 적용합니다.
 */
@Service
@RequiredArgsConstructor
public class PetLookUpServiceImpl implements PetLookUpService {

    private final PetRepository petRepository;

    @Override
    @Transactional(readOnly = true)
    public Pet findPet(PetIdentity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity cannot be null");
        }
        return petRepository.findByIdentity(identity)
            .orElseThrow(() -> new PetNotFoundException(identity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pet> findAllPets() {
        return petRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pet> findPetsByUserId(UserIdentity userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        return petRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Pet createPet(Pet pet) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        return petRepository.save(pet);
    }

    @Override
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