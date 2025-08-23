package com.puppytalk.user;

import java.util.List;

/**
 * 사용자 도메인 서비스
 * 사용자 관련 비즈니스 규칙과 정책을 담당
 */
public class UserDomainService {
    
    private final UserRepository userRepository;
    
    public UserDomainService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository must not be null");
        }
        this.userRepository = userRepository;
    }
    
    /**
     * 새로운 사용자를 등록한다.
     * 
     * @param username 사용자명
     * @param email 이메일
     * @return 저장된 사용자 ID
     * @throws DuplicateUserException 사용자명 또는 이메일이 중복된 경우
     */
    public UserId registerUser(String username, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자명은 필수입니다");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        
        validateUniqueUsername(username.trim());
        validateUniqueEmail(email.trim());
        
        User user = User.create(username, email);
        return userRepository.save(user);
    }
    
    /**
     * 사용자 ID로 사용자를 조회한다.
     * 
     * @param userId 사용자 ID
     * @return 사용자
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User findUserById(UserId userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    /**
     * 사용자명으로 사용자를 조회한다.
     * 
     * @param username 사용자명
     * @return 사용자
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }
        
        return userRepository.findByUsername(username.trim())
            .orElseThrow(() -> new UserNotFoundException(username));
    }
    
    /**
     * 모든 활성 사용자를 조회한다.
     * 
     * @return 활성 사용자 목록
     */
    public List<User> findActiveUsers() {
        return userRepository.findByStatus(UserStatus.ACTIVE);
    }
    
    /**
     * 이메일로 사용자를 조회한다.
     * 
     * @param email 이메일
     * @return 사용자
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        
        return userRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }
    
    /**
     * 사용자를 비활성화한다.
     * 
     * @param userId 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public void deactivateUser(UserId userId) {
        User user = findUserById(userId);
        user.deactivate();
        userRepository.save(user);
    }
    
    /**
     * 사용자를 활성화한다.
     * 
     * @param userId 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public void activateUser(UserId userId) {
        User user = findUserById(userId);
        user.activate();
        userRepository.save(user);
    }
    
    /**
     * 사용자명이 고유한지 검증한다.
     * 
     * @param username 사용자명
     * @throws DuplicateUserException 이미 존재하는 사용자명인 경우
     */
    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException(username);
        }
    }
    
    /**
     * 이메일이 고유한지 검증한다.
     * 
     * @param email 이메일
     * @throws DuplicateUserException 이미 존재하는 이메일인 경우
     */
    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateUserException.byEmail(email);
        }
    }
}