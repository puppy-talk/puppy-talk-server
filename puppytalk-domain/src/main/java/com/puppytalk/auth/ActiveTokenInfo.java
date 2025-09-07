package com.puppytalk.auth;

import com.puppytalk.user.UserId;
import java.time.LocalDateTime;

/**
 * 활성 토큰 정보를 나타내는 값 객체
 */
public record ActiveTokenInfo(
    UserId userId,
    String accessToken,
    LocalDateTime tokenExpiry,
    LocalDateTime issuedAt,
    String clientInfo
) {
    
    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(tokenExpiry);
    }
    
    /**
     * 토큰이 유효한지 확인 (만료되지 않은 상태)
     */
    public boolean isValid() {
        return !isExpired();
    }
    
    /**
     * 토큰 발급 후 경과 시간 (시간 단위)
     */
    public long getHoursSinceIssued() {
        return java.time.Duration.between(issuedAt, LocalDateTime.now()).toHours();
    }
}