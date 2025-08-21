package com.puppy.talk.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket 이벤트 리스너
 * 클라이언트 연결/해제 시 필요한 처리를 담당하며 사용자 세션 추적 기능을 제공
 * WebSocket Service 계층에서 실시간 연결 관리를 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final WebSocketSessionManager sessionManager;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        log.info("New WebSocket connection established: sessionId={}", sessionId);
        
        // 사용자 정보는 WebSocketAuthInterceptor에서 설정됨
        // 여기서는 연결 로깅만 수행
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket connection closed: sessionId={}", sessionId);
        
        // WebSocketSessionManager를 통해 세션 정리
        WebSocketSessionManager.UserSessionInfo sessionInfo = sessionManager.getSessionById(sessionId);
        if (sessionInfo != null) {
            log.info("Cleaned up session for user: userId={}, chatRoomId={}", 
                sessionInfo.userId(), sessionInfo.currentChatRoomId());
                
            // 채팅방에서 사용자 퇴장 처리
            if (sessionInfo.currentChatRoomId() != null) {
                handleUserLeftChatRoom(sessionInfo.userId(), sessionInfo.currentChatRoomId());
            }
        }
        
        // 세션 매니저에서 세션 제거
        sessionManager.removeSession(sessionId);
    }
    
    /**
     * 채팅방에서 사용자 퇴장 처리 로직
     */
    private void handleUserLeftChatRoom(Long userId, Long chatRoomId) {
        if (userId != null && chatRoomId != null) {
            log.info("Processing user exit from chat room: userId={}, chatRoomId={}", userId, chatRoomId);
            
            // 실제 운영에서는 다음과 같은 처리가 필요:
            // 1. 타이핑 상태 정리
            // 2. 마지막 활동 시간 업데이트
            // 3. 다른 사용자들에게 퇴장 알림 (필요한 경우)
            // 4. 활동 추적 서비스에 퇴장 이벤트 기록
            
            // 현재는 로깅만 수행하지만 확장 가능한 구조로 설계
        }
    }
    
    /**
     * 활성 세션 수 조회 (모니터링 목적)
     */
    public int getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }
    
    /**
     * 특정 채팅방의 활성 사용자 수 조회
     */
    public long getActiveChatRoomUsers(Long chatRoomId) {
        return sessionManager.getActiveChatRoomUsers(chatRoomId);
    }
}