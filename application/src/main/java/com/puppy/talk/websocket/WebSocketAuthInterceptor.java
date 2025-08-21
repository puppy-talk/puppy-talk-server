package com.puppy.talk.websocket;

import com.puppy.talk.auth.AuthService;
import com.puppy.talk.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * WebSocket 인증 및 권한 검사 인터셉터
 * Application Layer - WebSocket 연결과 메시지 전송에 대한 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private final AuthService authService;
    private final WebSocketSessionManager sessionManager;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            // CONNECT 명령 시 인증 검사
            if (StompCommand.CONNECT.equals(command)) {
                handleConnect(accessor);
            }
            
            // SEND 명령 시 권한 검사
            else if (StompCommand.SEND.equals(command)) {
                handleSend(accessor);
            }
        }
        
        return message;
    }
    
    private void handleConnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        log.debug("WebSocket CONNECT attempt: sessionId={}", sessionId);
        
        // JWT 토큰 검증
        String authToken = accessor.getFirstNativeHeader("Authorization");
        if (authToken == null || authToken.trim().isEmpty()) {
            log.warn("WebSocket connection attempt without authorization token: sessionId={}", sessionId);
            throw new SecurityException("Authorization token is required for WebSocket connection");
        }
        
        // Bearer 토큰에서 실제 토큰 추출
        String token = extractBearerToken(authToken);
        if (token == null) {
            log.warn("WebSocket connection attempt with invalid token format: sessionId={}", sessionId);
            throw new SecurityException("Invalid token format. Expected 'Bearer <token>'");
        }
        
        // 토큰 유효성 검증 및 사용자 정보 추출
        Optional<User> userOpt = authService.validateToken(token);
        if (userOpt.isEmpty()) {
            log.warn("WebSocket connection attempt with invalid token: sessionId={}", sessionId);
            throw new SecurityException("Invalid or expired authentication token");
        }
        
        User user = userOpt.get();
        
        // 세션에 사용자 정보 저장
        accessor.getSessionAttributes().put("userId", user.identity().id());
        accessor.getSessionAttributes().put("username", user.username());
        
        // 세션 매니저에 사용자 등록
        sessionManager.registerUserSession(sessionId, user.identity().id());
        
        log.info("WebSocket authentication successful: userId={}, username={}, sessionId={}", 
            user.identity().id(), user.username(), sessionId);
    }
    
    private void handleSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        
        log.debug("WebSocket SEND to destination: {} from session: {}", destination, sessionId);
        
        Long userId = getUserIdFromSession(accessor);
        if (userId == null) {
            log.warn("WebSocket message send attempt without authenticated user: sessionId={}", sessionId);
            throw new SecurityException("User authentication required for message sending");
        }
        
        if (destination != null && destination.startsWith("/app/chat/")) {
            validateChatRoomAccess(destination, userId, sessionId);
        }
    }
    
    /**
     * 채팅방 접근 권한을 검증합니다.
     * 현재는 기본적인 형식 검증만 수행하고, 실제 권한 검사는 비즈니스 로직에서 처리합니다.
     */
    private void validateChatRoomAccess(String destination, Long userId, String sessionId) {
        // 채팅방 ID 추출
        String[] pathParts = destination.split("/");
        if (pathParts.length < 4) {
            throw new SecurityException("Invalid chat destination format: " + destination);
        }
        
        try {
            Long chatRoomId = Long.parseLong(pathParts[3]);
            log.debug("User attempting to send message to chatRoom: userId={}, chatRoomId={}", userId, chatRoomId);
            
            // 기본적인 형식 검증 통과 후 세션 추적
            sessionManager.trackUserJoinedChatRoom(sessionId, userId, chatRoomId);
            
            log.debug("Chat room access format validation passed: userId={}, chatRoomId={}", userId, chatRoomId);
            
            // 실제 비즈니스 로직의 권한 검사는 ChatApplicationService에서 수행됨
            // 예: ChatApplicationService.sendMessageToPet() 에서 Pet 소유권 확인
            
        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid chat room ID format: " + pathParts[3]);
        }
    }
    
    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     */
    private String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 세션에서 사용자 ID를 추출합니다.
     */
    private Long getUserIdFromSession(StompHeaderAccessor accessor) {
        Object userId = accessor.getSessionAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }
}