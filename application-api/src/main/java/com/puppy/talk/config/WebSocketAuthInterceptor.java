package com.puppy.talk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 인증 및 권한 검사 인터셉터
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
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
        
        // TODO: 실제 프로덕션에서는 JWT 토큰 검증 등의 인증 로직 추가
        // 현재는 개발 단계이므로 기본적인 로깅만 수행
        
        // 예시: Authorization 헤더에서 토큰 추출 및 검증
        // String authToken = accessor.getFirstNativeHeader("Authorization");
        // if (!isValidToken(authToken)) {
        //     throw new SecurityException("Invalid authentication token");
        // }
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
    
    // TODO: 토큰 검증 메서드
    // private boolean isValidToken(String token) {
    //     return token != null && !token.trim().isEmpty();
    // }
    
    // TODO: 채팅방 권한 검사 메서드  
    // private boolean hasPermissionToChat(String userId, String chatRoomId) {
    //     return true; // 임시로 모든 요청 허용
    // }
}