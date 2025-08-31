package com.puppytalk.user;

import com.puppytalk.support.validation.Preconditions;
import java.util.List;

/**
 * 사용자 도메인 서비스
 * 사용자 관련 비즈니스 규칙과 정책을 담당
 */
public class UserDomainService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * UserDomainService 생성자
     * 
     * @param userRepository 사용자 저장소 (null 불가)
     * @param passwordEncoder 비밀번호 암호화기 (null 불가)
     * @throws IllegalArgumentException 파라미터가 null인 경우
     */
    public UserDomainService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = Preconditions.requireNonNull(userRepository, "UserRepository");
        this.passwordEncoder = Preconditions.requireNonNull(passwordEncoder, "PasswordEncoder");
    }
    
    /**
     * 새로운 사용자를 등록한다.
     * 
     * @param username 사용자명
     * @param email 이메일
     * @param rawPassword 원시 비밀번호
     * @return 저장된 사용자 ID
     * @throws DuplicateUserException 사용자명 또는 이메일이 중복된 경우
     */
    public UserId registerUser(String username, String email, String rawPassword) {
        Preconditions.requireNonBlank(username, "Username");
        Preconditions.requireNonBlank(email, "Email");
        Preconditions.requireNonBlank(rawPassword, "Password");
        
        validateUniqueUsername(username.trim());
        validateUniqueEmail(email.trim());
        
        // 도메인 서비스에서 비밀번호 암호화 수행
        String encryptedPassword = passwordEncoder.encode(rawPassword);
        User user = User.create(username, email, encryptedPassword);
        return userRepository.save(user);
    }
    
    /**
     * 사용자 ID로 사용자를 조회한다.
     * 
     * @param userId 사용자 ID
     * @return 사용자
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User getUserById(UserId userId) {
        Preconditions.requireValidId(userId, "UserId");
        
        return userRepository.findById(userId)
            .orElseThrow(() -> UserNotFoundException.byId(userId));
    }
    
    /**
     * 사용자명으로 사용자를 조회한다.
     * 
     * @param username 사용자명
     * @return 사용자
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User getUserByUsername(String username) {
        Preconditions.requireNonBlank(username, "Username");
        
        return userRepository.findByUsername(username.trim())
            .orElseThrow(() -> UserNotFoundException.byUsername(username));
    }
    
    /**
     * 모든 활성 사용자를 조회한다.
     * 
     * @return 활성 사용자 목록
     */
    public List<User> findActiveUsers() {
        return userRepository.findActiveUsers();
    }
    
    /**
     * 이메일로 사용자를 조회한다.
     * 
     * @param email 이메일
     * @return 사용자
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User getUserByEmail(String email) {
        Preconditions.requireNonBlank(email, "Email");
        
        return userRepository.findByEmail(email.trim().toLowerCase())
            .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }
    
    /**
     * 사용자를 삭제한다 (소프트 삭제).
     * 
     * @param userId 사용자 ID
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public void deleteUser(UserId userId) {
        User user = getUserById(userId);
        User deletedUser = user.withDeletedStatus();
        userRepository.save(deletedUser);
    }
    
    /**
     * 비밀번호 검증
     * 
     * @param user 사용자
     * @param rawPassword 평문 비밀번호
     * @return 비밀번호가 일치하면 true
     */
    public boolean checkPassword(User user, String rawPassword) {
        Preconditions.requireNonBlank(rawPassword, "Password");
        return passwordEncoder.matches(rawPassword, user.password());
    }
    
    /**
     * 비밀번호 변경
     * 
     * @param userId 사용자 ID
     * @param newRawPassword 새로운 평문 비밀번호
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public void changePassword(UserId userId, String newRawPassword) {
        Preconditions.requireNonBlank(newRawPassword, "Password");
        
        User user = getUserById(userId);
        String newEncryptedPassword = passwordEncoder.encode(newRawPassword);
        User updatedUser = user.withPassword(newEncryptedPassword);
        userRepository.save(updatedUser);
    }
    
    
    /**
     * 사용자명이 고유한지 검증한다.
     * 
     * @param username 사용자명
     * @throws DuplicateUserException 이미 존재하는 사용자명인 경우
     */
    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("이미 존재하는 사용자명입니다: " + username);
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
            throw new DuplicateUserException("이미 존재하는 이메일입니다: " + email);
        }
    }
}