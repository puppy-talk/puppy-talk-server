package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 사용자 등록 관련 처리를 담당하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationHandler {
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 등록 입력값을 검증합니다.
     */
    public boolean validateRegistrationInput(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Registration attempt with empty username");
            return false;
        }
        if (email == null || email.trim().isEmpty()) {
            log.warn("Registration attempt with empty email");
            return false;
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            log.warn("Registration attempt with invalid password for username: {}", username);
            return false;
        }
        return true;
    }
    
    /**
     * 사용자가 이미 존재하는지 확인합니다.
     */
    public boolean checkUserExists(String username, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Registration attempt with duplicate username: {}", username);
            return true;
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration attempt with duplicate email: {}", email);
            return true;
        }
        return false;
    }
    
    /**
     * 새로운 사용자를 생성합니다.
     */
    public Optional<AuthResult> createNewUser(String username, String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        
        User newUser = new User(UserIdentity.of(0L), username, email, hashedPassword);
        User savedUser = userRepository.save(newUser);
        
        String token = jwtTokenProvider.createToken(savedUser.identity().id(), savedUser.username());
        log.info("Successful registration for user: {} (ID: {})", username, savedUser.identity().id());
        
        return Optional.of(new AuthResult(token, savedUser));
    }
}