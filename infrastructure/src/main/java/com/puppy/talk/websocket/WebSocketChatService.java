package com.puppy.talk.websocket;

import com.puppy.talk.notification.RealtimeNotificationException;
import com.puppy.talk.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket을 통한 실시간 채팅 서비스
 * 
 * WebSocket STOMP를 통한 실시간 메시징, 타이핑 상태, 
 * 읽음 확인 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketChatService {
    
    private static final String TOPIC_CHAT_PREFIX = "/topic/chat/";
    private static final String TOPIC_TYPING_SUFFIX = "/typing";
    private static final String TOPIC_READ_SUFFIX = "/read";
    private static final String TOPIC_SYSTEM_SUFFIX = "/system";
    private static final String USER_QUEUE_PREFIX = "/user/";
    private static final String USER_QUEUE_SUFFIX = "/queue/messages";
    private static final String UNKNOWN_ID = "unknown";
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void broadcastMessage(ChatMessage message) {
        validateMessage(message, "ChatMessage cannot be null");
        
        try {
            String destination = buildChatDestination(message.chatRoomId().id());
            sendMessageToDestination(destination, message, "message");
            
        } catch (Exception e) {
            log.error("Failed to broadcast message: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast message", e);
        }
    }
    
    public void broadcastTypingStatus(ChatMessage typingMessage) {
        if (typingMessage == null) {
            log.warn("Cannot broadcast null typing message");
            return; // Non-critical operation, continue gracefully
        }
        
        try {
            String destination = buildTypingDestination(typingMessage.chatRoomId().id());
            sendMessageToDestination(destination, typingMessage, "typing status");
            
            log.debug("Broadcasting typing status to {}: user={}, type={}", 
                destination, typingMessage.userId().id(), typingMessage.messageType());
                
        } catch (Exception e) {
            log.error("Failed to broadcast typing status: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast typing status", e);
        }
    }
    
    public void broadcastReadReceipt(ChatMessage readReceiptMessage) {
        validateMessage(readReceiptMessage, "Read receipt message cannot be null");
        
        try {
            String destination = buildReadDestination(readReceiptMessage.chatRoomId().id());
            sendMessageToDestination(destination, readReceiptMessage, "read receipt");
            
            log.debug("Broadcasting read receipt to {}: user={}", 
                destination, readReceiptMessage.userId().id());
                
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt: {}", e.getMessage(), e);
            throw new RealtimeNotificationException("Failed to broadcast read receipt", e);
        }
    }
    
    public void sendToUser(UserIdentity userId, ChatMessage message) {
        if (!validateUserMessage(userId, message)) {
            return; // Validation failed, log already printed
        }
        
        try {
            String destination = buildUserDestination(userId.id());
            sendMessageToDestination(destination, message, "private message");
            
            log.debug("Sending private message to user {}: {}", userId.id(), message.content());
            
        } catch (Exception e) {
            log.error("Failed to send private message to user {}: {}", userId.id(), e.getMessage(), e);
            // Non-critical error for private messages, don't throw
        }
    }
    
    public void broadcastSystemMessage(ChatMessage systemMessage) {
        validateMessage(systemMessage, "System message cannot be null");
        
        try {
            String destination = buildSystemDestination(systemMessage.chatRoomId().id());
            sendMessageToDestination(destination, systemMessage, "system message");
            
            log.debug("Broadcasting system message to {}: {}", destination, systemMessage.content());
            
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
        
        String userId = extractSafeId(joinMessage.userId());
        String chatRoomId = extractSafeId(joinMessage.chatRoomId());
        
        try {
            broadcastSystemMessage(joinMessage);
            log.info("User {} joined chat room {}", userId, chatRoomId);
        } catch (Exception e) {
            log.error("Failed to broadcast user joined message for user={}, chatRoom={}: {}", 
                userId, chatRoomId, e.getMessage(), e);
        }
    }
    
    /**
     * 사용자 퇴장 알림  
     */
    public void notifyUserLeft(ChatMessage leaveMessage) {
        if (leaveMessage == null) {
            log.warn("Cannot notify user left with null message");
            return;
        }
        
        String userId = extractSafeId(leaveMessage.userId());
        String chatRoomId = extractSafeId(leaveMessage.chatRoomId());
        
        try {
            broadcastSystemMessage(leaveMessage);
            log.info("User {} left chat room {}", userId, chatRoomId);
        } catch (Exception e) {
            log.error("Failed to broadcast user left message for user={}, chatRoom={}: {}", 
                userId, chatRoomId, e.getMessage(), e);
        }
    }

    // === Helper Methods for WebSocket Operations ===
    
    private void validateMessage(ChatMessage message, String errorMessage) {
        if (message == null) {
            log.warn("Cannot process null message");
            throw new RealtimeNotificationException(errorMessage);
        }
    }
    
    private boolean validateUserMessage(UserIdentity userId, ChatMessage message) {
        if (userId == null) {
            log.warn("Cannot send message to null userId");
            return false;
        }
        if (message == null) {
            log.warn("Cannot send null message to user {}", userId.id());
            return false;
        }
        return true;
    }
    
    private void sendMessageToDestination(String destination, ChatMessage message, String messageType) {
        log.debug("Broadcasting {} to {}: {}", messageType, destination, message.content());
        messagingTemplate.convertAndSend(destination, message);
    }
    
    // === Destination Builders ===
    
    private String buildChatDestination(Long chatRoomId) {
        return TOPIC_CHAT_PREFIX + chatRoomId;
    }
    
    private String buildTypingDestination(Long chatRoomId) {
        return TOPIC_CHAT_PREFIX + chatRoomId + TOPIC_TYPING_SUFFIX;
    }
    
    private String buildReadDestination(Long chatRoomId) {
        return TOPIC_CHAT_PREFIX + chatRoomId + TOPIC_READ_SUFFIX;
    }
    
    private String buildSystemDestination(Long chatRoomId) {
        return TOPIC_CHAT_PREFIX + chatRoomId + TOPIC_SYSTEM_SUFFIX;
    }
    
    private String buildUserDestination(Long userId) {
        return USER_QUEUE_PREFIX + userId + USER_QUEUE_SUFFIX;
    }
    
    // === Safe ID Extraction ===
    
    private String extractSafeId(UserIdentity identity) {
        return identity != null ? String.valueOf(identity.id()) : UNKNOWN_ID;
    }
    
    private String extractSafeId(com.puppy.talk.chat.ChatRoomIdentity identity) {
        return identity != null ? String.valueOf(identity.id()) : UNKNOWN_ID;
    }
}