package com.puppytalk.auth.dto.response;

import com.puppytalk.auth.ActiveTokenInfo;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 활성 토큰 목록 결과 DTO
 */
public record ActiveTokensResult(
    List<TokenSessionInfo> activeSessions,
    int totalCount
) {
    
    public static ActiveTokensResult from(List<ActiveTokenInfo> activeTokens) {
        List<TokenSessionInfo> sessions = activeTokens.stream()
            .map(TokenSessionInfo::from)
            .toList();
        
        return new ActiveTokensResult(sessions, sessions.size());
    }
    
    /**
     * 토큰 세션 정보
     */
    public record TokenSessionInfo(
        String tokenId,  // 토큰의 앞 10자리
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        String clientInfo,
        boolean isCurrentSession,
        long hoursSinceIssued
    ) {
        
        public static TokenSessionInfo from(ActiveTokenInfo tokenInfo) {
            return new TokenSessionInfo(
                tokenInfo.accessToken().substring(0, Math.min(10, tokenInfo.accessToken().length())),
                tokenInfo.issuedAt(),
                tokenInfo.tokenExpiry(),
                tokenInfo.clientInfo(),
                false,  // 현재 세션 여부는 별도 로직 필요
                tokenInfo.getHoursSinceIssued()
            );
        }
    }
}