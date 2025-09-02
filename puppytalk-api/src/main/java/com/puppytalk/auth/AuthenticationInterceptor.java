package com.puppytalk.auth;

import com.puppytalk.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 토큰 기반 인증을 처리하는 인터셉터
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";
    
    private final AuthenticationDomainService authenticationDomainService;
    
    public AuthenticationInterceptor(AuthenticationDomainService authenticationDomainService) {
        this.authenticationDomainService = authenticationDomainService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 토큰 추출
        String token = extractToken(request);

        // 2. 토큰 유효성 검사
        authenticationDomainService.validateToken(token);

        // 3. 토큰으로 사용자 정보 조회
        User user = authenticationDomainService.getUserFromToken(token);

        // 4. request 객체에 사용자 정보 등록
        request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
        logger.debug("JWT token validated successfully for user: {}", user.getUsername());
        return true;
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        logger.debug("No JWT token found in request headers");
        throw new InvalidTokenException("토큰이 필요합니다");
    }
}
