package com.puppytalk.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * 상태별 사용자 목록 조회
     */
    List<UserJpaEntity> findByStatus(UserStatus status);
    
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
    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);
}