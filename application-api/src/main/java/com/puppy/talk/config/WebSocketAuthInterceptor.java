package com.puppy.talk.config;

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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private final AuthService authService;
    
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
        
        log.info("WebSocket authentication successful: userId={}, username={}, sessionId={}", 
            user.identity().id(), user.username(), sessionId);
    }
    
    private void handleSend(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        
        log.debug("WebSocket SEND to destination: {} from session: {}", destination, sessionId);
        
        // TODO: 메시지 전송 권한 검사
        // 예: 사용자가 해당 채팅방에 참여 권한이 있는지 확인
        
        if (destination != null && destination.startsWith("/app/chat/")) {
            // 채팅방 ID 추출
            String[] pathParts = destination.split("/");
            if (pathParts.length >= 4) {
                String chatRoomId = pathParts[3];
                log.debug("User attempting to send message to chatRoom: {}", chatRoomId);
                
                // TODO: 실제 권한 검사 로직
                // if (!hasPermissionToChat(userId, chatRoomId)) {
                //     throw new SecurityException("Access denied to chat room: " + chatRoomId);
                // }
            }
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