package com.puppytalk.pet;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.UserId;
import java.util.List;

/**
 * 반려동물 도메인 서비스
 * 반려동물 관련 비즈니스 규칙과 정책을 담당
 */
public class PetDomainService {
    
    private final PetRepository petRepository;
    
    /**
     * PetDomainService 생성자
     * 
     * @param petRepository 반려동물 저장소 (null 불가)
     * @throws IllegalArgumentException petRepository가 null인 경우
     */
    public PetDomainService(PetRepository petRepository) {
        this.petRepository = Preconditions.requireNonNull(petRepository, "PetRepository");
    }

    /**
     * 반려동물을 ID와 소유자 ID로 조회한다.
     * 
     * @param petId 반려동물 ID (null 불가, 저장된 ID)
     * @param ownerId 소유자 ID (null 불가, 저장된 ID)
     * @return 반려동물
     * @throws IllegalArgumentException ID가 null이거나 유효하지 않은 경우
     * @throws PetNotFoundException 반려동물이 존재하지 않거나 소유자가 다른 경우
     */
    public Pet getPet(PetId petId, UserId ownerId) {
        Preconditions.requireValidId(petId, "PetId");
        Preconditions.requireValidId(ownerId, "OwnerId");

        return petRepository.findByIdAndOwnerId(petId, ownerId)
            .orElseThrow(() -> new PetNotFoundException(petId));
    }
    
    /**
     * 새로운 반려동물을 생성한다.
     * 
     * @param ownerId 소유자 ID (null 불가, 저장된 ID)
     * @param petName 반려동물 이름 (null이나 공백 불가, 최대 50자)
     * @param persona 반려동물 페르소나 (null이나 공백 불가, 최대 500자)
     * @throws IllegalArgumentException 매개변수가 유효하지 않은 경우
     * @throws RuntimeException 생성 실패 시
     */
    public void createPet(UserId ownerId, String petName, String persona) {
        Preconditions.requireValidId(ownerId, "OwnerId");
        Preconditions.requireNonBlank(petName, "PetName");
        Preconditions.requireNonBlank(persona, "Persona");

        Pet pet = Pet.create(ownerId, petName, persona);
        petRepository.create(pet);
    }

    /**
     * 소유자의 모든 반려동물 목록을 조회한다.
     * 
     * @param ownerId 소유자 ID (null 불가, 저장된 ID)
     * @return 반려동물 목록 (빈 목록 가능)
     * @throws IllegalArgumentException ownerId가 유효하지 않은 경우
     * @throws RuntimeException 조회 실패 시
     */
    public List<Pet> findPetList(UserId ownerId) {
        Preconditions.requireValidId(ownerId, "OwnerId");
        return petRepository.findByOwnerId(ownerId);
    }
    
    /**
     * 반려동물을 삭제한다 (소프트 삭제).
     * 소유자의 비정상 접근을 방지하기 위해 소유자 ID를 확인한다.
     * 
     * @param petId 반려동물 ID (null 불가, 저장된 ID)
     * @param ownerId 소유자 ID (null 불가, 저장된 ID)
     * @throws IllegalArgumentException ID가 유효하지 않은 경우
     * @throws PetNotFoundException 반려동물이 존재하지 않거나 소유자가 다른 경우
     * @throws IllegalStateException 이미 삭제된 반려동물인 경우
     */
    public void deletePet(PetId petId, UserId ownerId) {
        Pet pet = getPet(petId, ownerId);
        Pet deletedPet = pet.withDeletedStatus();
        petRepository.delete(deletedPet);
    }
}