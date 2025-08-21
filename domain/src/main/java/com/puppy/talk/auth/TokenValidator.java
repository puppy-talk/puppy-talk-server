package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JWT 토큰 검증을 담당하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenValidator {
    
    private static final int TOKEN_SUBSTRING_LENGTH = 10;
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    /**
     * 토큰 형식을 검증합니다.
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return true;
    }
    
    /**
     * 토큰 내용을 검증합니다.
     */
    public boolean isValidTokenContent(String token) {
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
    
    /**
     * 토큰에서 사용자 정보를 추출합니다.
     */
    public Optional<User> extractUserFromToken(String token) {
        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            return userRepository.findByIdentity(UserIdentity.of(userId));
        } catch (Exception e) {
            log.warn("Error validating token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}