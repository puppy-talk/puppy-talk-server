package com.puppytalk.auth;

import com.puppytalk.support.validation.Preconditions;
import com.puppytalk.user.User;
import com.puppytalk.user.UserId;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.UserNotFoundException;
import java.util.List;

/**
 * 인증 도메인 서비스
 * 사용자 로그인과 토큰 관리를 담당
 */
public class AuthenticationDomainService {
    
    private final UserDomainService userDomainService;
    private final TokenProvider tokenProvider;
    private final TokenStore tokenStore;
    
    public AuthenticationDomainService(UserDomainService userDomainService, 
                                     TokenProvider tokenProvider,
                                     TokenStore tokenStore) {
        this.userDomainService = Preconditions.requireNonNull(userDomainService, "UserDomainService");
        this.tokenProvider = Preconditions.requireNonNull(tokenProvider, "TokenProvider");
        this.tokenStore = Preconditions.requireNonNull(tokenStore, "TokenStore");
    }
    
    /**
     * 사용자 로그인을 처리한다
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @return JWT 토큰 정보
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     * @throws InvalidCredentialsException 인증 정보가 올바르지 않은 경우
     */
    public JwtToken login(String username, String password) {
        Preconditions.requireNonBlank(username, "Username");
        Preconditions.requireNonBlank(password, "Password");
        
        User user = userDomainService.getUserByUsername(username.trim());
        
        if (!userDomainService.checkPassword(user, password)) {
            throw new InvalidCredentialsException("인증 정보가 올바르지 않습니다");
        }
        
        // 사용자 활동시간 업데이트
        userDomainService.updateLastActiveTime(user.getId());
        
        JwtToken token = tokenProvider.generateToken(user.getId(), user.getUsername());
        
        // 토큰을 Redis에 저장
        tokenStore.storeToken(
            user.getId(),
            token.accessToken(),
            token.expiresAt()
        );
        
        return token;
    }
    
    /**
     * 토큰을 검증하고 사용자 정보를 반환한다
     * 
     * @param accessToken 액세스 토큰
     * @return 사용자 정보
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     * @throws UserNotFoundException 사용자가 존재하지 않는 경우
     */
    public User validateTokenAndGetUser(String accessToken) {
        Preconditions.requireNonBlank(accessToken, "AccessToken");
        
        // Redis에서 토큰 활성 상태 확인
        if (!tokenStore.isTokenActive(accessToken)) {
            throw InvalidTokenException.invalidToken();
        }
        
        if (!tokenProvider.validateToken(accessToken)) {
            throw InvalidTokenException.invalidToken();
        }
        
        var userId = tokenProvider.getUserIdFromToken(accessToken);
        return userDomainService.getUserById(userId);
    }
    
    
    /**
     * 사용자의 모든 토큰을 무효화한다 (전체 로그아웃)
     * 
     * @param userId 사용자 ID
     */
    public void logout(UserId userId) {
        Preconditions.requireValidId(userId, "UserId");
        tokenStore.invalidateAllTokensForUser(userId);
    }
    
    /**
     * 특정 토큰을 무효화한다 (단일 세션 로그아웃)
     * 
     * @param accessToken 액세스 토큰
     */
    public void logoutToken(String accessToken) {
        Preconditions.requireNonBlank(accessToken, "AccessToken");
        tokenStore.invalidateToken(accessToken);
    }
    
    /**
     * 사용자의 활성 토큰 목록을 조회한다
     * 
     * @param userId 사용자 ID
     * @return 활성 토큰 정보 목록
     */
    public List<ActiveTokenInfo> getActiveTokens(UserId userId) {
        Preconditions.requireValidId(userId, "UserId");
        return tokenStore.getActiveTokensForUser(userId);
    }
}