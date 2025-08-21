package com.puppy.talk.auth;

import com.puppy.talk.user.User;
import java.util.Optional;

/**
 * 인증 관련 조회 서비스 인터페이스
 */
public interface AuthLookUpService {
    
    /**
     * 사용자 로그인을 처리합니다.
     * 
     * @param username 사용자명
     * @param password 패스워드
     * @return 인증 결과 (성공시 토큰과 사용자 정보)
     */
    Optional<AuthResult> login(String username, String password);
    
    /**
     * 사용자 등록을 처리합니다.
     * 
     * @param username 사용자명
     * @param email 이메일 주소
     * @param password 패스워드
     * @return 등록 결과 (성공시 토큰과 사용자 정보)
     */
    Optional<AuthResult> register(String username, String email, String password);
    
    /**
     * JWT 토큰을 검증하고 사용자 정보를 반환합니다.
     * 
     * @param token JWT 토큰
     * @return 검증된 사용자 정보
     */
    Optional<User> validateToken(String token);
    
    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     * 
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    Optional<Long> getUserIdFromToken(String token);
}