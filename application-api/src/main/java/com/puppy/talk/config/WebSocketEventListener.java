package com.puppy.talk.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 이벤트 리스너
 * 클라이언트 연결/해제 시 필요한 처리를 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final SimpMessageSendingOperations messagingTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId").toString();
        log.info("New WebSocket connection established: sessionId={}", sessionId);
        
        // 필요시 사용자 세션 정보를 추적하여 저장
        // 예: Redis에 세션 정보 저장, 활성 사용자 수 카운트 등
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket connection closed: sessionId={}", sessionId);
        
        // 세션 종료 시 필요한 정리 작업
        // 예: 활성 채팅방에서 사용자 제거, 타이핑 상태 정리 등
        
        // TODO: 사용자가 어느 채팅방에 있었는지 추적하여 퇴장 알림 전송
        // 현재는 간단한 로깅만 수행
    }
}