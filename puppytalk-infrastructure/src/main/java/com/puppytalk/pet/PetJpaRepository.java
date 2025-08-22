package com.puppytalk.pet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PetJpaRepository extends JpaRepository<PetJpaEntity, Long> {
    
    /**
     * ID와 소유자 ID로 반려동물 조회 (소유권 필터링)
     */
    Optional<PetJpaEntity> findByIdAndOwnerId(Long id, Long ownerId);
    
    /**
     * 소유자의 모든 반려동물 조회 (삭제된 것 제외)
     */
    List<PetJpaEntity> findByOwnerIdAndStatusNot(Long ownerId, PetStatus status);
    
    /**
     * 소유자의 활성 반려동물 조회
     */
    List<PetJpaEntity> findByOwnerIdAndStatus(Long ownerId, PetStatus status);
    
    /**
     * 소유자의 반려동물 개수 조회 (삭제된 것 제외)
     */
    @Query("SELECT COUNT(p) FROM PetJpaEntity p WHERE p.ownerId = :ownerId AND p.status != :excludeStatus")
    long countByOwnerIdAndStatusNot(@Param("ownerId") Long ownerId, @Param("excludeStatus") PetStatus excludeStatus);
    
    /**
     * 소유자별 반려동물 개수 조회 (활성 상태만)
     */
    long countByOwnerIdAndStatus(Long ownerId, PetStatus status);
}
