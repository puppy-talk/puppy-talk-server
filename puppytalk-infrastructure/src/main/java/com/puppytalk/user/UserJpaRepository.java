package com.puppytalk.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     */
    Optional<UserJpaEntity> findByUsername(String username);
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<UserJpaEntity> findByEmail(String email);
    
    /**
     * 활성 사용자 목록 조회 (삭제되지 않은 사용자)
     */
    List<UserJpaEntity> findByIsDeletedFalse();
    
    /**
     * 삭제된 사용자 목록 조회
     */
    List<UserJpaEntity> findByIsDeletedTrue();
    
    /**
     * 사용자명 존재 여부 확인
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 활성 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE u.isDeleted = false")
    long countActiveUsers();
    
    /**
     * 삭제된 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE u.isDeleted = true")
    long countDeletedUsers();
    
    /**
     * 특정 시간 이전에 활동한 비활성 사용자 목록 조회 (삭제되지 않은 사용자만)
     */
    List<UserJpaEntity> findByLastActiveAtBeforeAndIsDeletedFalse(LocalDateTime cutoffTime);
}