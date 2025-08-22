package com.puppytalk.pet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 반려동물 생성 정책 서비스
 * 
 * 반려동물 생성과 관련된 비즈니스 규칙을 관리합니다.
 * 정책의 변경이나 확장에 대해 유연하게 대응할 수 있도록 분리되었습니다.
 */
@Component
public class PetCreationPolicy {
    
    private final int maxPetsPerUser;
    private final PetRepository petRepository;
    private final PersonaRepository personaRepository;
    
    public PetCreationPolicy(
            @Value("${puppytalk.pet.max-per-user:5}") int maxPetsPerUser,
            PetRepository petRepository,
            PersonaRepository personaRepository
    ) {
        this.maxPetsPerUser = maxPetsPerUser;
        this.petRepository = petRepository;
        this.personaRepository = personaRepository;
    }
    
    /**
     * 사용자가 반려동물을 생성할 수 있는지 검증
     */
    public void validateUserCanCreatePet(Long ownerId, PersonaId personaId) {
        validatePetCountLimit(ownerId);
        validatePersonaExists(personaId);
    }
    
    /**
     * 사용자의 반려동물 개수 제한 확인
     */
    private void validatePetCountLimit(Long ownerId) {
        long currentPetCount = petRepository.countByOwnerId(ownerId);
        if (currentPetCount >= maxPetsPerUser) {
            throw new MaxPetsExceededException(ownerId, maxPetsPerUser, (int) currentPetCount);
        }
    }
    
    /**
     * 페르소나 존재 여부 확인
     */
    private void validatePersonaExists(PersonaId personaId) {
        if (!personaRepository.existsById(personaId)) {
            throw new PersonaNotFoundException(personaId);
        }
    }
    
    /**
     * 최대 반려동물 개수 조회 (외부 노출용)
     */
    public int getMaxPetsPerUser() {
        return maxPetsPerUser;
    }
}