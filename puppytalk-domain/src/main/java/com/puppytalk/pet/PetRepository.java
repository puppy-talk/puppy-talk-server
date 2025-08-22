package com.puppytalk.pet;
import java.util.List;
import java.util.Optional;

/**
 * 반려동물 저장소 인터페이스
 */
public interface PetRepository {
    
    /**
     * 반려동물 저장
     */
    void save(Pet pet);
    
    /**
     * ID로 반려동물 조회
     */
    Optional<Pet> findById(PetId id);
    
    /**
     * 소유자의 모든 반려동물 조회 (삭제된 것 제외)
     */
    List<Pet> findByOwnerId(Long ownerId);
    
    /**
     * 소유자의 활성 반려동물 조회
     */
    List<Pet> findActiveByOwnerId(Long ownerId);
    
    /**
     * 반려동물 존재 여부 확인
     */
    boolean existsById(PetId id);
    
    /**
     * 소유자의 반려동물 개수 조회 (삭제된 것 제외)
     */
    long countByOwnerId(Long ownerId);
    
    /**
     * 특정 페르소나를 사용하는 반려동물 개수 조회
     */
    long countByPersonaId(PersonaId personaId);
}