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
    
    /**
     * 반려동물 ID와 소유자 ID로 반려동물을 조회한다.
     * 쿼리 레벨에서 소유권을 필터링하여 효율적이고 안전하다.
     * 
     * @param petId 반려동물 ID
     * @param ownerId 소유자 ID
     * @return 소유권이 확인된 반려동물
     * @throws PetNotFoundException 반려동물이 존재하지 않거나 소유권이 없는 경우
     */
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
    
    public void createPet(UserId ownerId, String petName, String petPersona) {
        Pet pet = Pet.create(ownerId, petName, petPersona);
        petRepository.save(pet);
    }
    
    /**
     * 소유자의 반려동물 목록을 조회한다.
     * 소유자 ID로만 조회하므로 별도의 소유권 검증이 불필요하다.
     * 
     * @param ownerId 소유자 ID
     * @return 해당 소유자의 반려동물 목록
     */
    public List<Pet> findPetList(UserId ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("OwnerId must not be null");
        }
        return petRepository.findByOwnerId(ownerId);
    }
    
    public void deletePet(PetId petId, UserId ownerId) {
        Pet pet = findPet(petId, ownerId);
        pet.delete();
        petRepository.save(pet);
    }
}