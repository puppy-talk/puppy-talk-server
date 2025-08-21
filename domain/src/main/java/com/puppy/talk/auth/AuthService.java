package com.puppy.talk.auth;

import com.puppy.talk.user.User;
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
public class AuthService implements AuthLookUpService {
    
    private final LoginValidator loginValidator;
    private final RegistrationHandler registrationHandler;
    private final TokenValidator tokenValidator;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자 로그인을 처리합니다.
     * 
     * @param username 사용자명
     * @param password 패스워드
     * @return 인증 결과 (성공시 토큰과 사용자 정보)
     */
    @Transactional(readOnly = true)
    public Optional<AuthResult> login(String username, String password) {
        if (!loginValidator.validateLoginInput(username, password)) {
            return Optional.empty();
        }

        String trimmedUsername = username.trim();
        Optional<User> userOpt = loginValidator.findUserForLogin(trimmedUsername);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        
        if (!loginValidator.validatePassword(password, user.passwordHash(), trimmedUsername)) {
            return Optional.empty();
        }

        return loginValidator.generateAuthResult(user, trimmedUsername);
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
        if (!registrationHandler.validateRegistrationInput(username, email, password)) {
            return Optional.empty();
        }

        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim();

        if (registrationHandler.checkUserExists(trimmedUsername, trimmedEmail)) {
            return Optional.empty();
        }

        return registrationHandler.createNewUser(trimmedUsername, trimmedEmail, password);
    }

    /**
     * JWT 토큰을 검증하고 사용자 정보를 반환합니다.
     * 
     * @param token JWT 토큰
     * @return 검증된 사용자 정보
     */
    @Transactional(readOnly = true)
    public Optional<User> validateToken(String token) {
        if (!tokenValidator.isValidTokenFormat(token)) {
            return Optional.empty();
        }

        if (!tokenValidator.isValidTokenContent(token)) {
            return Optional.empty();
        }

        return tokenValidator.extractUserFromToken(token);
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
}