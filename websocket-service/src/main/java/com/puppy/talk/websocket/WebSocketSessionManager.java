package com.puppy.talk.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * WebSocket 세션 관리자
 * 사용자 세션과 채팅방 간의 매핑을 관리합니다.
 */
@Slf4j
@Component
public class WebSocketSessionManager {
    
    private final ConcurrentMap<String, UserSessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, String> userIdToSessionId = new ConcurrentHashMap<>();
    
    /**
     * 사용자 세션을 등록합니다.
     */
    public void registerUserSession(String sessionId, Long userId) {
        UserSessionInfo sessionInfo = new UserSessionInfo(userId, null);
        activeSessions.put(sessionId, sessionInfo);
        userIdToSessionId.put(userId, sessionId);
        
        log.info("User session registered: userId={}, sessionId={}", userId, sessionId);
    }
    
    /**
     * 사용자가 채팅방에 참여함을 추적합니다.
     */
    public void trackUserJoinedChatRoom(String sessionId, Long userId, Long chatRoomId) {
        UserSessionInfo sessionInfo = activeSessions.get(sessionId);
        if (sessionInfo != null) {
            UserSessionInfo updatedInfo = new UserSessionInfo(userId, chatRoomId);
            activeSessions.put(sessionId, updatedInfo);
            log.debug("User joined chat room: userId={}, chatRoomId={}, sessionId={}", 
                userId, chatRoomId, sessionId);
        } else {
            log.warn("Attempt to track chat room for non-existent session: sessionId={}", sessionId);
        }
    }
    
    /**
     * 세션을 제거합니다.
     */
    public void removeSession(String sessionId) {
        UserSessionInfo sessionInfo = activeSessions.remove(sessionId);
        if (sessionInfo != null) {
            userIdToSessionId.remove(sessionInfo.userId());
            log.info("User session removed: userId={}, sessionId={}", 
                sessionInfo.userId(), sessionId);
        }
    }
    
    /**
     * 사용자 ID로 세션 정보를 조회합니다.
     */
    public UserSessionInfo getSessionByUserId(Long userId) {
        String sessionId = userIdToSessionId.get(userId);
        if (sessionId != null) {
            return activeSessions.get(sessionId);
        }
        return null;
    }
    
    /**
     * 세션 ID로 세션 정보를 조회합니다.
     */
    public UserSessionInfo getSessionById(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * 현재 활성 세션 수를 반환합니다.
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
    
    /**
     * 특정 채팅방의 활성 사용자 수를 조회합니다.
     */
    public long getActiveChatRoomUsers(Long chatRoomId) {
        return activeSessions.values().stream()
            .filter(session -> chatRoomId.equals(session.currentChatRoomId()))
            .count();
    }
    
    /**
     * 사용자 세션 정보를 담는 레코드
     */
    public record UserSessionInfo(Long userId, Long currentChatRoomId) {}
}