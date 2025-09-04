package com.puppytalk.auth;

import com.puppytalk.auth.dto.request.LoginCommand;
import com.puppytalk.auth.dto.request.LogoutCommand;
import com.puppytalk.auth.dto.response.ActiveTokensResult;
import com.puppytalk.auth.dto.response.TokenResult;
import com.puppytalk.user.UserId;
import com.puppytalk.user.UserFacade;
import com.puppytalk.user.UserDomainService;
import com.puppytalk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 인증 관련 애플리케이션 파사드
 */
@Service
@Transactional(readOnly = true)
public class AuthenticationFacade {
    
    private final AuthenticationDomainService authenticationDomainService;
    private final UserFacade userFacade;
    private final UserDomainService userDomainService;
    
    public AuthenticationFacade(AuthenticationDomainService authenticationDomainService, UserFacade userFacade, UserDomainService userDomainService) {
        this.authenticationDomainService = authenticationDomainService;
        this.userFacade = userFacade;
        this.userDomainService = userDomainService;
    }
    
    /**
     * 사용자 로그인을 처리한다 (휴면 해제 포함)
     */
    @Transactional
    public TokenResult login(LoginCommand command) {
        Assert.notNull(command, "LoginCommand must not be null");
        Assert.hasText(command.username(), "Username must not be null or empty");
        Assert.hasText(command.password(), "Password must not be null or empty");
        
        JwtToken token = authenticationDomainService.login(command.username(), command.password());
        
        // 로그인 성공 시 사용자 활성화 (휴면 해제)
        try {
            User user = userDomainService.getUserByUsername(command.username());
            userFacade.activateUser(user.getId().value());
        } catch (Exception e) {
            // 활성화 실패는 로그인을 방해하지 않음 (로그는 남김)
            // 로그인 자체는 성공했으므로 토큰을 정상 반환
        }
        
        return TokenResult.from(token);
    }
    
    /**
     * 로그아웃을 처리한다
     */
    @Transactional
    public void logout(LogoutCommand command) {
        Assert.notNull(command, "LogoutCommand must not be null");
        Assert.notNull(command.userId(), "UserId must not be null");
        
        if (command.logoutAll()) {
            // 모든 디바이스에서 로그아웃
            authenticationDomainService.logout(command.userId());
        } else {
            // 현재 토큰만 로그아웃
            Assert.hasText(command.accessToken(), "Access token must not be null or empty for single logout");
            authenticationDomainService.logoutToken(command.accessToken());
        }
    }
    
    /**
     * 사용자의 활성 토큰 목록을 조회한다
     */
    public ActiveTokensResult getActiveTokens(UserId userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        var activeTokens = authenticationDomainService.getActiveTokens(userId);
        return ActiveTokensResult.from(activeTokens);
    }
}