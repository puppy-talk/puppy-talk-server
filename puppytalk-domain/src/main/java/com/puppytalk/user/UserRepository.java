package com.puppytalk.user;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소 인터페이스
 */
public interface UserRepository {
    
    /**
     * 사용자를 저장하고 ID를 반환한다.
     */
    UserId save(User user);
    
    /**
     * ID로 사용자를 조회한다.
     */
    Optional<User> findById(UserId userId);
    
    /**
     * 사용자명으로 사용자를 조회한다.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 사용자를 조회한다.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 활성 사용자 목록을 조회한다 (삭제되지 않은 사용자).
     */
    List<User> findActiveUsers();
    
    /**
     * 삭제된 사용자 목록을 조회한다.
     */
    List<User> findDeletedUsers();
    
    /**
     * 사용자명 존재 여부를 확인한다.
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 존재 여부를 확인한다.
     */
    boolean existsByEmail(String email);
}