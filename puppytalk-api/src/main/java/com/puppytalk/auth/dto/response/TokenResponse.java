package com.puppytalk.auth.dto.response;

import com.puppytalk.auth.dto.response.TokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 토큰 응답 DTO
 */
@Schema(description = "토큰 응답")
public record TokenResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    
    @Schema(description = "토큰 만료 시간", example = "2024-01-01T12:00:00")
    LocalDateTime expiresAt
) {
    
    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(
            result.accessToken(),
            result.expiresAt()
        );
    }
}