package com.puppytalk.auth;

import com.puppytalk.user.UserId;

/**
 * JWT 토큰 생성 및 검증을 담당하는 포트 인터페이스
 */
public interface TokenProvider {
    
    /**
     * 사용자를 위한 JWT 토큰을 생성한다
     * 
     * @param userId 사용자 ID
     * @param username 사용자명
     * @return JWT 토큰 정보
     */
    JwtToken generateToken(UserId userId, String username);
    
    /**
     * 액세스 토큰에서 사용자 ID를 추출한다
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 ID
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    UserId getUserIdFromToken(String accessToken);
    
    /**
     * 액세스 토큰이 유효한지 검증한다
     * 
     * @param accessToken 액세스 토큰
     * @return 유효한 경우 true
     */
    boolean validateToken(String accessToken);
}