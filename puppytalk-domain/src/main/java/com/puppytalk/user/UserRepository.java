package com.puppytalk.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소 인터페이스
 * 
 * <p>사용자 엔티티의 영속성을 담당하는 리포지토리입니다.
 * 모든 메서드는 null-safe하며, 입력 매개변수는 null이 될 수 없습니다.
 * 
 * @since 1.0
 */
public interface UserRepository {
    
    /**
     * 사용자를 저장하고 ID를 반환한다.
     * 
     * @param user 저장할 사용자 (null 불가)
     * @return 저장된 사용자의 ID
     * @throws IllegalArgumentException user가 null인 경우
     * @throws RuntimeException 저장 실패 시
     */
    UserId save(User user);
    
    /**
     * ID로 사용자를 조회한다.
     * 
     * @param userId 사용자 ID (null 불가, 저장된 ID)
     * @return 사용자 (존재하지 않으면 Optional.empty())
     * @throws IllegalArgumentException userId가 null이거나 유효하지 않은 경우
     */
    Optional<User> findById(UserId userId);
    
    /**
     * 사용자명으로 사용자를 조회한다.
     * 
     * @param username 사용자명 (null이나 공백 불가)
     * @return 사용자 (존재하지 않으면 Optional.empty())
     * @throws IllegalArgumentException username이 null이거나 공백인 경우
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 사용자를 조회한다.
     * 
     * @param email 이메일 (null이나 공백 불가)
     * @return 사용자 (존재하지 않으면 Optional.empty())
     * @throws IllegalArgumentException email이 null이거나 공백인 경우
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 활성 사용자 목록을 조회한다 (삭제되지 않은 사용자).
     * 
     * @return 활성 사용자 목록 (빈 목록 가능)
     * @throws RuntimeException 조회 실패 시
     */
    List<User> findActiveUsers();
    
    /**
     * 삭제된 사용자 목록을 조회한다.
     * 
     * @return 삭제된 사용자 목록 (빈 목록 가능)
     * @throws RuntimeException 조회 실패 시
     */
    List<User> findDeletedUsers();
    
    /**
     * 사용자명 존재 여부를 확인한다.
     * 
     * @param username 사용자명 (null이나 공백 불가)
     * @return 존재하면 true
     * @throws IllegalArgumentException username이 null이거나 공백인 경우
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 존재 여부를 확인한다.
     * 
     * @param email 이메일 (null이나 공백 불가)
     * @return 존재하면 true
     * @throws IllegalArgumentException email이 null이거나 공백인 경우
     */
    boolean existsByEmail(String email);
    
    /**
     * 특정 시간 이전에 활동한 비활성 사용자 ID 목록을 조회한다.
     * 
     * @param cutoffTime 기준 시간 (이 시간 이전에 활동한 사용자들을 비활성으로 간주)
     * @return 비활성 사용자 ID 목록
     * @throws IllegalArgumentException cutoffTime이 null인 경우
     */
    List<Long> findInactiveUsers(LocalDateTime cutoffTime);
}