package com.puppytalk.pet;
import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;

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
     * ID와 소유자 ID로 반려동물 조회 (소유권 필터링)
     */
    Optional<Pet> findByIdAndOwnerId(PetId id, UserId ownerId);
    
    /**
     * 소유자의 모든 반려동물 조회 (삭제된 것 제외)
     */
    List<Pet> findByOwnerId(UserId ownerId);
    
    /**
     * 소유자의 활성 반려동물 조회
     */
    List<Pet> findActiveByOwnerId(UserId ownerId);
    
    /**
     * 반려동물 존재 여부 확인
     */
    boolean existsById(PetId id);
    
    /**
     * 소유자의 반려동물 개수 조회 (삭제된 것 제외)
     */
    long countByOwnerId(UserId ownerId);
    
}