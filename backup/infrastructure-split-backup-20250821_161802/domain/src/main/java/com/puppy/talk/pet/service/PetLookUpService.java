package com.puppy.talk.pet.service;

import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.user.UserIdentity;
import java.util.List;

/**
 * 펫 관련 도메인 서비스
 * 
 * 펫 관리와 관련된 순수한 비즈니스 로직을 담당합니다.
 * 인프라스트럭처 세부사항에 의존하지 않습니다.
 */
public interface PetLookUpService {

    Pet findPet(PetIdentity identity);

    List<Pet> findAllPets();

    List<Pet> findPetsByUserId(UserIdentity userId);

    Pet createPet(Pet pet);

    void deletePet(PetIdentity identity);
}