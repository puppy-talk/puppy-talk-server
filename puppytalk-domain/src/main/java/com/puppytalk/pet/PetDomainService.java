package com.puppytalk.pet;

import com.puppytalk.pet.exception.UnauthorizedPetAccessException;

/**
 * 반려동물 도메인 서비스
 * 반려동물 관련 비즈니스 규칙과 정책을 담당
 */
public class PetDomainService {
    
    private final PetRepository petRepository;
    
    public PetDomainService(PetRepository petRepository) {
        if (petRepository == null) {
            throw new IllegalArgumentException("PetRepository must not be null");
        }
        this.petRepository = petRepository;
    }
    
    /**
     * 반려동물 ID와 소유자 ID로 반려동물을 조회하고 소유권을 검증한다.
     * 
     * @param petId 반려동물 ID
     * @param ownerId 소유자 ID
     * @return 소유권이 검증된 반려동물
     * @throws PetNotFoundException 반려동물이 존재하지 않는 경우
     * @throws UnauthorizedPetAccessException 소유권이 없는 경우
     */
    public Pet findPetWithOwnershipValidation(PetId petId, Long ownerId) {
        if (petId == null) {
            throw new IllegalArgumentException("PetId must not be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("OwnerId must not be null");
        }
        
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));
        
        validateOwnership(pet, ownerId);
        
        return pet;
    }
    
    /**
     * 반려동물의 소유권을 검증한다.
     * 
     * @param pet 반려동물
     * @param ownerId 소유자 ID
     * @throws UnauthorizedPetAccessException 소유권이 없는 경우
     */
    private void validateOwnership(Pet pet, Long ownerId) {
        if (!pet.isOwnedBy(ownerId)) {
            throw new UnauthorizedPetAccessException(pet.getId().value(), ownerId);
        }
    }
}