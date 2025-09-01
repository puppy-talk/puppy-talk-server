package com.puppytalk.auth;

import com.puppytalk.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
        // OPTIONS 요청은 통과
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        String token = extractTokenFromRequest(request);
        
        if (!StringUtils.hasText(token)) {
            logger.debug("No JWT token found in request headers");
            setUnauthorizedResponse(response, "토큰이 필요합니다");
            return false;
        }
        
        try {
            User user = authenticationDomainService.validateTokenAndGetUser(token);
            request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
            logger.debug("JWT token validated successfully for user: {}", user.getUsername());
            return true;
        } catch (InvalidTokenException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            setUnauthorizedResponse(response, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage(), e);
            setUnauthorizedResponse(response, "인증 처리 중 오류가 발생했습니다");
            return false;
        }
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    private void setUnauthorizedResponse(HttpServletResponse response, String message) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            String jsonResponse = String.format(
                "{\"success\":false,\"message\":\"%s\",\"data\":null}",
                message
            );
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            logger.error("Error writing unauthorized response: {}", e.getMessage(), e);
        }
    }
}