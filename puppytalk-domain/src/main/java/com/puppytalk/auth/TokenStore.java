package com.puppytalk.auth;

import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JWT 토큰 저장소 포트 인터페이스
 * 토큰의 생명주기 관리와 보안 기능을 담당
 */
public interface TokenStore {
    
    /**
     * 활성 토큰을 저장한다
     * 
     * @param userId 사용자 ID
     * @param accessToken 액세스 토큰
     * @param tokenExpiry 토큰 만료시간
     */
    void storeToken(UserId userId, String accessToken, LocalDateTime tokenExpiry);
    
    /**
     * 토큰이 활성 상태인지 확인한다
     * 
     * @param accessToken 액세스 토큰
     * @return 활성 상태인 경우 true
     */
    boolean isTokenActive(String accessToken);
    
    /**
     * 사용자의 모든 토큰을 무효화한다 (로그아웃)
     * 
     * @param userId 사용자 ID
     */
    void invalidateAllTokensForUser(UserId userId);
    
    /**
     * 특정 토큰을 무효화한다
     * 
     * @param accessToken 액세스 토큰
     */
    void invalidateToken(String accessToken);
    
    /**
     * 사용자의 활성 토큰 정보를 조회한다
     * 
     * @param userId 사용자 ID
     * @return 활성 토큰 정보 목록
     */
    List<ActiveTokenInfo> getActiveTokensForUser(UserId userId);
    
    /**
     * 만료된 토큰들을 정리한다
     * 
     * @return 정리된 토큰 개수
     */
    int cleanupExpiredTokens();
    
    /**
     * 토큰으로 사용자 ID를 조회한다
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 ID (토큰이 활성 상태인 경우)
     */
    Optional<UserId> getUserIdByToken(String accessToken);
}