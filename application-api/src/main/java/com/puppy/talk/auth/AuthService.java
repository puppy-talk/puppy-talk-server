package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 인증 및 권한 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 사용자 로그인을 처리합니다.
     */
    @Transactional(readOnly = true)
    public Optional<AuthResult> login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Login attempt with empty username");
            return Optional.empty();
        }
        if (password == null || password.trim().isEmpty()) {
            log.warn("Login attempt with empty password for username: {}", username);
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("Login attempt with non-existent username: {}", username);
            return Optional.empty();
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            log.warn("Login attempt with invalid password for username: {}", username);
            return Optional.empty();
        }

        String token = jwtTokenProvider.createToken(user.identity().id(), user.username());
        log.info("Successful login for user: {} (ID: {})", username, user.identity().id());
        
        return Optional.of(new AuthResult(token, user));
    }

    /**
     * 사용자 등록을 처리합니다.
     */
    @Transactional
    public Optional<AuthResult> register(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            log.warn("Registration attempt with empty username");
            return Optional.empty();
        }
        if (email == null || email.trim().isEmpty()) {
            log.warn("Registration attempt with empty email");
            return Optional.empty();
        }
        if (password == null || password.length() < 6) {
            log.warn("Registration attempt with invalid password for username: {}", username);
            return Optional.empty();
        }

        // 사용자명 중복 확인
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Registration attempt with duplicate username: {}", username);
            return Optional.empty();
        }

        // 이메일 중복 확인
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration attempt with duplicate email: {}", email);
            return Optional.empty();
        }

        // 패스워드 해싱
        String hashedPassword = passwordEncoder.encode(password);
        
        // 사용자 생성
        User newUser = User.builder()
            .username(username.trim())
            .email(email.trim())
            .passwordHash(hashedPassword)
            .build();

        User savedUser = userRepository.save(newUser);
        String token = jwtTokenProvider.createToken(savedUser.identity().id(), savedUser.username());
        
        log.info("Successful registration for user: {} (ID: {})", username, savedUser.identity().id());
        
        return Optional.of(new AuthResult(token, savedUser));
    }

    /**
     * JWT 토큰을 검증하고 사용자 정보를 반환합니다.
     */
    @Transactional(readOnly = true)
    public Optional<User> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return Optional.empty();
        }

        if (jwtTokenProvider.isTokenExpired(token)) {
            log.debug("Token expired for token: {}", token.substring(0, 10) + "...");
            return Optional.empty();
        }

        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            return userRepository.findByIdentity(UserIdentity.of(userId));
        } catch (Exception e) {
            log.warn("Error validating token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
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

    /**
     * 인증 결과를 담는 레코드
     */
    public record AuthResult(
        String token,
        User user
    ) {}
}