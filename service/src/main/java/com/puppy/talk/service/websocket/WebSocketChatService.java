package com.puppy.talk.service.websocket;

import com.puppy.talk.infrastructure.notification.RealtimeNotificationException;
import com.puppy.talk.infrastructure.notification.RealtimeNotificationPort;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.model.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket을 통한 실시간 채팅 서비스
 * RealtimeNotificationPort의 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketChatService implements RealtimeNotificationPort {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void broadcastMessage(ChatMessage message) {
        if (message == null) {
            log.warn("Cannot broadcast null message");
            throw new RealtimeNotificationException("ChatMessage cannot be null");
        }
        
        try {
            String destination = "/topic/chat/" + message.chatRoomId().id();
            
            log.debug("Broadcasting message to {}: {}", destination, message.content());
            
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to broadcast message: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast message", e);
        }
    }
    
    @Override
    public void broadcastTypingStatus(ChatMessage typingMessage) {
        if (typingMessage == null) {
            log.warn("Cannot broadcast null typing message");
            return;
        }
        
        try {
            String destination = "/topic/chat/" + typingMessage.chatRoomId().id() + "/typing";
            
            log.debug("Broadcasting typing status to {}: user={}, type={}", 
                destination, typingMessage.userId().id(), typingMessage.messageType());
            
            messagingTemplate.convertAndSend(destination, typingMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast typing status: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast typing status", e);
        }
    }
    
    @Override
    public void broadcastReadReceipt(ChatMessage readReceiptMessage) {
        if (readReceiptMessage == null) {
            log.warn("Cannot broadcast null read receipt message");
            throw new RealtimeNotificationException("Read receipt message cannot be null");
        }
        
        try {
            String destination = "/topic/chat/" + readReceiptMessage.chatRoomId().id() + "/read";
            
            log.debug("Broadcasting read receipt to {}: user={}", 
                destination, readReceiptMessage.userId().id());
            
            messagingTemplate.convertAndSend(destination, readReceiptMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast read receipt", e);
        }
    }
    
    /**
     * 특정 사용자에게만 메시지 전송
     */
    public void sendToUser(UserIdentity userId, ChatMessage message) {
        if (userId == null) {
            log.warn("Cannot send message to null userId");
            return;
        }
        if (message == null) {
            log.warn("Cannot send null message to user {}", userId.id());
            return;
        }
        
        try {
            String destination = "/user/" + userId.id() + "/queue/messages";
            
            log.debug("Sending private message to user {}: {}", userId.id(), message.content());
            
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to send private message to user {}: {}", userId.id(), e.getMessage(), e);
        }
    }
    
    @Override
    public void broadcastSystemMessage(ChatMessage systemMessage) {
        if (systemMessage == null) {
            log.warn("Cannot broadcast null system message");
            throw new RealtimeNotificationException("System message cannot be null");
        }
        
        try {
            String destination = "/topic/chat/" + systemMessage.chatRoomId().id() + "/system";
            
            log.debug("Broadcasting system message to {}: {}", destination, systemMessage.content());
            
            messagingTemplate.convertAndSend(destination, systemMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast system message: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast system message", e);
        }
    }
    
    /**
     * 사용자 입장 알림
     */
    public void notifyUserJoined(ChatMessage joinMessage) {
        if (joinMessage == null) {
            log.warn("Cannot notify user joined with null message");
            return;
        }
        
        broadcastSystemMessage(joinMessage);
        
        log.info("User {} joined chat room {}", 
            joinMessage.userId().id(), joinMessage.chatRoomId().id());
    }
    
    /**
     * 사용자 퇴장 알림  
     */
    public void notifyUserLeft(ChatMessage leaveMessage) {
        if (leaveMessage == null) {
            log.warn("Cannot notify user left with null message");
            return;
        }
        
        broadcastSystemMessage(leaveMessage);
        
        log.info("User {} left chat room {}", 
            leaveMessage.userId().id(), leaveMessage.chatRoomId().id());
    }
}