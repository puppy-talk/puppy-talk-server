package com.puppytalk.pet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 반려동물 애플리케이션 서비스 (Facade 패턴)
 * 
 * 반려동물 관련 유스케이스를 조합하고 트랜잭션을 관리합니다.
 */
@Service
@Transactional
public class PetFacade {
    
    private final PetRepository petRepository;
    private final PersonaRepository personaRepository;
    
    // 한 사용자당 최대 반려동물 개수 제한
    private static final int MAX_PETS_PER_USER = 5;
    
    public PetFacade(PetRepository petRepository, PersonaRepository personaRepository) {
        this.petRepository = petRepository;
        this.personaRepository = personaRepository;
    }
    
    /**
     * 반려동물 생성
     */
    public Pet createPet(Long ownerId, String petName, PersonaId personaId) {
        // 비즈니스 규칙 검증
        validatePetCreation(ownerId, personaId);
        
        // 페르소나 조회
        Persona persona = personaRepository.findById(personaId)
            .orElseThrow(() -> new PersonaNotFoundException(personaId));
        
        // 반려동물 생성
        Pet pet = Pet.create(ownerId, petName, persona);
        
        // 저장
        return petRepository.save(pet);
    }
    
    /**
     * 반려동물 활성화
     */
    public void activatePet(PetId petId, Long ownerId) {
        Pet pet = findPetByIdAndOwner(petId, ownerId);
        pet.activate();
        petRepository.save(pet);
    }
    
    /**
     * 반려동물 비활성화
     */
    public void deactivatePet(PetId petId, Long ownerId) {
        Pet pet = findPetByIdAndOwner(petId, ownerId);
        pet.deactivate();
        petRepository.save(pet);
    }
    
    /**
     * 반려동물 삭제 (소프트 삭제)
     */
    public void deletePet(PetId petId, Long ownerId) {
        Pet pet = findPetByIdAndOwner(petId, ownerId);
        pet.delete();
        petRepository.save(pet);
    }
    
    /**
     * 반려동물이 채팅 가능한지 확인
     */
    @Transactional(readOnly = true)
    public boolean canPetChat(PetId petId, Long ownerId) {
        Pet pet = findPetByIdAndOwner(petId, ownerId);
        return pet.canChat();
    }
    
    /**
     * 사용자의 반려동물 개수 확인
     */
    @Transactional(readOnly = true)
    public long getUserPetCount(Long ownerId) {
        return petRepository.countByOwnerId(ownerId);
    }
    
    /**
     * 페르소나 사용 횟수 확인
     */
    @Transactional(readOnly = true)
    public long getPersonaUsageCount(PersonaId personaId) {
        return petRepository.countByPersonaId(personaId);
    }
    
    private void validatePetCreation(Long ownerId, PersonaId personaId) {
        // 사용자당 반려동물 개수 제한 확인
        long currentPetCount = petRepository.countByOwnerId(ownerId);
        if (currentPetCount >= MAX_PETS_PER_USER) {
            throw new IllegalStateException("반려동물은 최대 " + MAX_PETS_PER_USER + "마리까지 생성할 수 있습니다");
        }
        
        // 페르소나 존재 여부 확인
        if (!personaRepository.existsById(personaId)) {
            throw new PersonaNotFoundException(personaId);
        }
    }
    
    private Pet findPetByIdAndOwner(PetId petId, Long ownerId) {
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));
        
        if (!pet.isOwnedBy(ownerId)) {
            throw new IllegalArgumentException("해당 반려동물에 대한 권한이 없습니다");
        }
        
        return pet;
    }
}