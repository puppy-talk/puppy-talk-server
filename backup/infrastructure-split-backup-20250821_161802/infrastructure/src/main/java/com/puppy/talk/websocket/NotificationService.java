package com.puppy.talk.websocket;

import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.notification.RealtimeNotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실시간 알림 서비스
 * 
 * 레이어드 아키텍처에서 비즈니스 로직 계층에 위치하여
 * WebSocket을 통한 실시간 알림 기능을 제공합니다.
 * 
 * 📋 주요 책임:
 * ✅ 실시간 메시지 브로드캐스트
 * ✅ 타이핑 상태 알림
 * ✅ 읽음 확인 알림
 * ✅ 시스템 메시지 전송
 * ✅ 개인 메시지 전송
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {
    
    private final WebSocketChatService webSocketChatService;
    
    /**
     * 특정 채팅방의 모든 참여자에게 메시지를 실시간 브로드캐스트합니다.
     *
     * @param message 브로드캐스트할 채팅 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    public void broadcastMessage(ChatMessage message) {
        try {
            log.debug("Broadcasting message to chatRoom: {}", 
                message != null ? message.chatRoomId().id() : "null");
            webSocketChatService.broadcastMessage(message);
        } catch (Exception e) {
            log.error("Failed to broadcast message: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast message", e);
        }
    }
    
    /**
     * 특정 채팅방의 모든 참여자에게 타이핑 상태를 실시간 브로드캐스트합니다.
     *
     * @param typingMessage 타이핑 상태 메시지
     */
    public void broadcastTypingStatus(ChatMessage typingMessage) {
        try {
            log.debug("Broadcasting typing status to chatRoom: {}", 
                typingMessage != null ? typingMessage.chatRoomId().id() : "null");
            webSocketChatService.broadcastTypingStatus(typingMessage);
        } catch (Exception e) {
            log.warn("Failed to broadcast typing status: {}", e.getMessage(), e);
            // 타이핑 상태는 중요하지 않은 기능이므로 예외를 던지지 않음
        }
    }
    
    /**
     * 특정 채팅방의 모든 참여자에게 읽음 확인을 실시간 브로드캐스트합니다.
     *
     * @param readReceiptMessage 읽음 확인 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    public void broadcastReadReceipt(ChatMessage readReceiptMessage) {
        try {
            log.debug("Broadcasting read receipt to chatRoom: {}", 
                readReceiptMessage != null ? readReceiptMessage.chatRoomId().id() : "null");
            webSocketChatService.broadcastReadReceipt(readReceiptMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast read receipt", e);
        }
    }
    
    /**
     * 특정 채팅방에 시스템 메시지를 실시간 브로드캐스트합니다.
     *
     * @param systemMessage 시스템 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    public void broadcastSystemMessage(ChatMessage systemMessage) {
        try {
            log.debug("Broadcasting system message to chatRoom: {}", 
                systemMessage != null ? systemMessage.chatRoomId().id() : "null");
            webSocketChatService.broadcastSystemMessage(systemMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast system message: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast system message", e);
        }
    }
    
    /**
     * 특정 사용자에게 개인 메시지를 실시간 전송합니다.
     *
     * @param userId 전송할 사용자 식별자
     * @param message 전송할 채팅 메시지
     */
    public void sendToUser(UserIdentity userId, ChatMessage message) {
        try {
            log.debug("Sending private message to user: {}", 
                userId != null ? userId.id() : "null");
            webSocketChatService.sendToUser(userId, message);
        } catch (Exception e) {
            log.warn("Failed to send private message to user {}: {}", 
                userId != null ? userId.id() : "null", e.getMessage(), e);
            // 개인 메시지 실패는 중요하지 않으므로 예외를 던지지 않음
        }
    }
    
    /**
     * 사용자 입장 알림
     *
     * @param joinMessage 입장 메시지
     */
    public void notifyUserJoined(ChatMessage joinMessage) {
        try {
            log.debug("Notifying user joined to chatRoom: {}", 
                joinMessage != null ? joinMessage.chatRoomId().id() : "null");
            webSocketChatService.notifyUserJoined(joinMessage);
        } catch (Exception e) {
            log.warn("Failed to notify user joined: {}", e.getMessage(), e);
            // 사용자 입장 알림 실패는 중요하지 않으므로 예외를 던지지 않음
        }
    }
    
    /**
     * 사용자 퇴장 알림  
     *
     * @param leaveMessage 퇴장 메시지
     */
    public void notifyUserLeft(ChatMessage leaveMessage) {
        try {
            log.debug("Notifying user left to chatRoom: {}", 
                leaveMessage != null ? leaveMessage.chatRoomId().id() : "null");
            webSocketChatService.notifyUserLeft(leaveMessage);
        } catch (Exception e) {
            log.warn("Failed to notify user left: {}", e.getMessage(), e);
            // 사용자 퇴장 알림 실패는 중요하지 않으므로 예외를 던지지 않음
        }
    }
}