package com.puppytalk.auth.dto.response;

import com.puppytalk.auth.dto.response.ActiveTokensResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 활성 토큰 목록 응답 DTO
 */
@Schema(description = "활성 토큰 목록 응답")
public record ActiveTokensResponse(
    @Schema(description = "활성 세션 목록")
    List<TokenSessionResponse> activeSessions,
    
    @Schema(description = "전체 활성 세션 수", example = "3")
    int totalCount
) {
    
    public static ActiveTokensResponse from(ActiveTokensResult result) {
        List<TokenSessionResponse> sessions = result.activeSessions().stream()
            .map(TokenSessionResponse::from)
            .toList();
        
        return new ActiveTokensResponse(sessions, result.totalCount());
    }
    
    /**
     * 토큰 세션 응답 DTO
     */
    @Schema(description = "토큰 세션 정보")
    public record TokenSessionResponse(
        @Schema(description = "토큰 식별자 (일부)", example = "eyJhbGciOi...")
        String tokenId,
        
        @Schema(description = "토큰 발급 시간", example = "2024-01-01T10:00:00")
        LocalDateTime issuedAt,
        
        @Schema(description = "토큰 만료 시간", example = "2024-01-01T11:00:00")
        LocalDateTime expiresAt,
        
        @Schema(description = "클라이언트 정보", example = "Chrome on Windows")
        String clientInfo,
        
        @Schema(description = "현재 세션 여부", example = "true")
        boolean isCurrentSession,
        
        @Schema(description = "토큰 발급 후 경과 시간 (시간 단위)", example = "2")
        long hoursSinceIssued
    ) {
        
        public static TokenSessionResponse from(ActiveTokensResult.TokenSessionInfo sessionInfo) {
            return new TokenSessionResponse(
                sessionInfo.tokenId(),
                sessionInfo.issuedAt(),
                sessionInfo.expiresAt(),
                sessionInfo.clientInfo(),
                sessionInfo.isCurrentSession(),
                sessionInfo.hoursSinceIssued()
            );
        }
    }
}