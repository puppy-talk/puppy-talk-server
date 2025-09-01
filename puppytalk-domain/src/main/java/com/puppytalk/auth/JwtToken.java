package com.puppytalk.auth;

import com.puppytalk.support.validation.Preconditions;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JWT 토큰을 나타내는 값 객체
 */
public record JwtToken(
    String accessToken,
    LocalDateTime expiresAt
) {
    
    public static final int ACCESS_TOKEN_VALIDITY_HOURS = 24;  // 24시간으로 연장
    
    public JwtToken {
        Preconditions.requireNonBlank(accessToken, "AccessToken");
        Objects.requireNonNull(expiresAt, "ExpiresAt must not be null");
    }
    
    /**
     * 액세스 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * 토큰이 유효한지 확인
     */
    public boolean isValid() {
        return !isExpired();
    }
}