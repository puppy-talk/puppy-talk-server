package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import com.puppy.talk.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 로그인 관련 검증 및 처리를 담당하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginValidator {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 로그인 입력값을 검증합니다.
     */
    public boolean validateLoginInput(String username, String password) {
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
    
    /**
     * 로그인용 사용자를 조회합니다.
     */
    public Optional<User> findUserForLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("Login attempt with non-existent username: {}", username);
        }
        return userOpt;
    }
    
    /**
     * 패스워드를 검증합니다.
     */
    public boolean validatePassword(String password, String passwordHash, String username) {
        if (!passwordEncoder.matches(password, passwordHash)) {
            log.warn("Login attempt with invalid password for username: {}", username);
            return false;
        }
        return true;
    }
    
    /**
     * 인증 결과를 생성합니다.
     */
    public Optional<AuthResult> generateAuthResult(User user, String username) {
        String token = jwtTokenProvider.createToken(user.identity().id(), user.username());
        log.info("Successful login for user: {} (ID: {})", username, user.identity().id());
        return Optional.of(new AuthResult(token, user));
    }
}