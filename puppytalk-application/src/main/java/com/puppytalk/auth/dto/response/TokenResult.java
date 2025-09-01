package com.puppytalk.auth.dto.response;

import com.puppytalk.auth.JwtToken;
import java.time.LocalDateTime;

/**
 * 토큰 결과 DTO
 */
public record TokenResult(
    String accessToken,
    LocalDateTime expiresAt
) {
    
    public static TokenResult from(JwtToken token) {
        return new TokenResult(
            token.accessToken(),
            token.expiresAt()
        );
    }
}