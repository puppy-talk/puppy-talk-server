package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 인증 및 권한 관리 서비스
 * 
 * 보안 강화된 인증 서비스로 입력 검증, 로깅, 에러 처리를 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int TOKEN_SUBSTRING_LENGTH = 10;
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 로그인을 처리합니다.
     * 
     * @param username 사용자명
     * @param password 패스워드
     * @return 인증 결과 (성공시 토큰과 사용자 정보)
     */
    @Transactional(readOnly = true)
    public Optional<AuthResult> login(String username, String password) {
        if (!validateLoginInput(username, password)) {
            return Optional.empty();
        }

        String trimmedUsername = username.trim();
        Optional<User> userOpt = findUserForLogin(trimmedUsername);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        
        if (!validatePassword(password, user.passwordHash(), trimmedUsername)) {
            return Optional.empty();
        }

        return generateAuthResult(user, trimmedUsername);
    }

    /**
     * 사용자 등록을 처리합니다.
     * 
     * @param username 사용자명
     * @param email 이메일 주소
     * @param password 패스워드
     * @return 등록 결과 (성공시 토큰과 사용자 정보)
     */
    @Transactional
    public Optional<AuthResult> register(String username, String email, String password) {
        if (!validateRegistrationInput(username, email, password)) {
            return Optional.empty();
        }

        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim();

        if (checkUserExists(trimmedUsername, trimmedEmail)) {
            return Optional.empty();
        }

        return createNewUser(trimmedUsername, trimmedEmail, password);
    }

    /**
     * JWT 토큰을 검증하고 사용자 정보를 반환합니다.
     * 
     * @param token JWT 토큰
     * @return 검증된 사용자 정보
     */
    @Transactional(readOnly = true)
    public Optional<User> validateToken(String token) {
        if (!isValidTokenFormat(token)) {
            return Optional.empty();
        }

        if (!isValidTokenContent(token)) {
            return Optional.empty();
        }

        return extractUserFromToken(token);
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Optional<Long> getUserIdFromToken(String token) {
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                return Optional.empty();
            }
            return Optional.of(jwtTokenProvider.getUserIdFromToken(token));
        } catch (Exception e) {
            log.warn("Error extracting user ID from token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // === Helper Methods for Login ===
    
    private boolean validateLoginInput(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Login attempt with empty username");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("Login attempt with empty password for username: {}", username);
            return false;
        }
        return true;
    }
    
    private Optional<User> findUserForLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("Login attempt with non-existent username: {}", username);
        }
        return userOpt;
    }
    
    private boolean validatePassword(String password, String passwordHash, String username) {
        if (!passwordEncoder.matches(password, passwordHash)) {
            log.warn("Login attempt with invalid password for username: {}", username);
            return false;
        }
        return true;
    }
    
    private Optional<AuthResult> generateAuthResult(User user, String username) {
        String token = jwtTokenProvider.createToken(user.identity().id(), user.username());
        log.info("Successful login for user: {} (ID: {})", username, user.identity().id());
        return Optional.of(new AuthResult(token, user));
    }

    // === Helper Methods for Registration ===
    
    private boolean validateRegistrationInput(String username, String email, String password) {
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
    
    private boolean checkUserExists(String username, String email) {
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
    
    private Optional<AuthResult> createNewUser(String username, String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        
        User newUser = new User(UserIdentity.of(0L), username, email, hashedPassword);
        User savedUser = userRepository.save(newUser);
        
        String token = jwtTokenProvider.createToken(savedUser.identity().id(), savedUser.username());
        log.info("Successful registration for user: {} (ID: {})", username, savedUser.identity().id());
        
        return Optional.of(new AuthResult(token, savedUser));
    }

    // === Helper Methods for Token Validation ===
    
    private boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return true;
    }
    
    private boolean isValidTokenContent(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return false;
        }
        if (jwtTokenProvider.isTokenExpired(token)) {
            String tokenSubstring = token.length() > TOKEN_SUBSTRING_LENGTH 
                ? token.substring(0, TOKEN_SUBSTRING_LENGTH) + "..." 
                : token;
            log.debug("Token expired for token: {}", tokenSubstring);
            return false;
        }
        return true;
    }
    
    private Optional<User> extractUserFromToken(String token) {
        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            return userRepository.findByIdentity(UserIdentity.of(userId));
        } catch (Exception e) {
            log.warn("Error validating token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 인증 결과를 담는 레코드
     */
    public record AuthResult(
        String token,
        User user
    ) {}
}