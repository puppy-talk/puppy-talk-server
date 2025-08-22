package com.puppytalk.pet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final PetCreationPolicy creationPolicy;
    
    public PetFacade(PetRepository petRepository, 
                     PersonaRepository personaRepository,
                     PetCreationPolicy creationPolicy) {
        this.petRepository = petRepository;
        this.personaRepository = personaRepository;
        this.creationPolicy = creationPolicy;
    }
    
    /**
     * 반려동물 생성
     */
    public PetResult createPet(PetCreateCommand command) {
        PersonaId personaId = PersonaId.of(command.personaId());
        
        // 도메인 정책을 통한 비즈니스 규칙 검증
        creationPolicy.validateUserCanCreatePet(command.ownerId(), personaId);
        
        // 페르소나 조회
        Persona persona = personaRepository.findById(personaId)
            .orElseThrow(() -> new PersonaNotFoundException(personaId));
        
        // 반려동물 생성 (도메인 로직 위임)
        Pet pet = Pet.create(command.ownerId(), command.name(), persona);
        
        // 저장
        Pet savedPet = petRepository.save(pet);
        
        return PetResult.from(savedPet);
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
    
    /**
     * 페르소나 사용 횟수 확인 (API 레이어 호환용)
     */
    @Transactional(readOnly = true)
    public long getPersonaUsageCountByLongId(Long personaId) {
        PersonaId convertedPersonaId = PersonaId.of(personaId);
        return getPersonaUsageCount(convertedPersonaId);
    }
    
    /**
     * 사용자의 반려동물 목록 조회
     */
    @Transactional(readOnly = true)
    public PetsResult getUserPets(Long ownerId) {
        List<Pet> pets = petRepository.findByOwnerId(ownerId);
        return PetsResult.from(pets);
    }
    
    /**
     * 사용자의 반려동물 목록 조회 (Command 사용)
     */
    @Transactional(readOnly = true)
    public PetsResult getUserPets(UserPetsQueryCommand command) {
        return getUserPets(command.ownerId());
    }
    
    /**
     * 반려동물 상세 조회
     */
    @Transactional(readOnly = true)
    public PetResult getPet(PetId petId, Long ownerId) {
        Pet pet = findPetByIdAndOwner(petId, ownerId);
        return PetResult.from(pet);
    }
    
    /**
     * 반려동물 상세 조회 (API 레이어 호환용)
     */
    @Transactional(readOnly = true)
    public PetResult getPetByLongId(Long petId, Long ownerId) {
        PetId convertedPetId = PetId.of(petId);
        return getPet(convertedPetId, ownerId);
    }
    
    /**
     * 반려동물 상세 조회 (Command 사용)
     */
    @Transactional(readOnly = true)
    public PetResult getPet(PetQueryCommand command) {
        return getPetByLongId(command.petId(), command.ownerId());
    }
    
    
    /**
     * 반려동물 삭제 (API 레이어 호환용)
     */
    public void deletePetByLongId(Long petId, Long ownerId) {
        PetId convertedPetId = PetId.of(petId);
        deletePet(convertedPetId, ownerId);
    }
    
    /**
     * 반려동물 삭제 (Command 사용)
     */
    public void deletePet(PetStatusCommand command) {
        deletePetByLongId(command.petId(), command.ownerId());
    }
    
    
    /**
     * 반려동물 조회 및 소유권 검증
     * 
     * 도메인별 예외를 사용하여 더 명확한 에러 핸들링
     */
    private Pet findPetByIdAndOwner(PetId petId, Long ownerId) {
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));
        
        if (!pet.isOwnedBy(ownerId)) {
            throw new UnauthorizedPetAccessException(petId.value(), ownerId);
        }
        
        return pet;
    }
}