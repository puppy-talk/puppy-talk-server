package com.puppytalk.shared.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 전역 로깅 필터
 * 모든 HTTP 요청에 requestId와 userId를 MDC에 설정
 */
@Component
@Order(1)
public class LoggingFilter implements Filter {
    
    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String REQUEST_URI = "requestUri";
    private static final String REQUEST_METHOD = "requestMethod";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // 요청 ID 생성 (8자리 UUID)
            String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            MDC.put(REQUEST_ID, requestId);
            
            // 요청 정보 MDC 설정
            MDC.put(REQUEST_URI, httpRequest.getRequestURI());
            MDC.put(REQUEST_METHOD, httpRequest.getMethod());
            
            // 사용자 ID 추가 (인증된 경우)
            String userId = extractUserId(httpRequest);
            if (userId != null && !userId.isEmpty()) {
                MDC.put(USER_ID, userId);
            }
            
            chain.doFilter(request, response);
            
        } finally {
            // 요청 완료 후 MDC 정리
            MDC.clear();
        }
    }
    
    /**
     * HTTP 요청에서 사용자 ID 추출
     * TODO: 실제 인증 시스템 연동 후 구현
     */
    private String extractUserId(HttpServletRequest request) {
        // Authorization 헤더에서 JWT 토큰 추출 후 사용자 ID 파싱
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // TODO: JWT 토큰에서 userId 추출 로직 구현
            return null; // 임시로 null 반환
        }
        
        // 또는 세션에서 사용자 ID 추출
        Object userId = request.getSession(false) != null ? 
                       request.getSession().getAttribute("userId") : null;
        
        return userId != null ? userId.toString() : null;
    }
}