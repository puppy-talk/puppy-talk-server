package com.puppy.talk.service.websocket;

import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.model.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket을 통한 실시간 채팅 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketChatService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 특정 채팅방의 모든 참여자에게 메시지 브로드캐스트
     */
    public void broadcastMessage(ChatMessage message) {
        if (message == null) {
            log.warn("Cannot broadcast null message");
            throw new NullPointerException("ChatMessage cannot be null");
        }
        
        try {
            String destination = "/topic/chat/" + message.chatRoomId().id();
            
            log.debug("Broadcasting message to {}: {}", destination, message.content());
            
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to broadcast message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 채팅방의 모든 참여자에게 타이핑 상태 브로드캐스트
     */
    public void broadcastTyping(ChatMessage typingMessage) {
        try {
            String destination = "/topic/chat/" + typingMessage.chatRoomId().id() + "/typing";
            
            log.debug("Broadcasting typing status to {}: user={}, type={}", 
                destination, typingMessage.userId().id(), typingMessage.messageType());
            
            messagingTemplate.convertAndSend(destination, typingMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast typing status: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 채팅방의 모든 참여자에게 읽음 확인 브로드캐스트
     */
    public void broadcastReadReceipt(ChatMessage readMessage) {
        try {
            String destination = "/topic/chat/" + readMessage.chatRoomId().id() + "/read";
            
            log.debug("Broadcasting read receipt to {}: user={}", 
                destination, readMessage.userId().id());
            
            messagingTemplate.convertAndSend(destination, readMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 사용자에게만 메시지 전송
     */
    public void sendToUser(UserIdentity userId, ChatMessage message) {
        try {
            String destination = "/user/" + userId.id() + "/queue/messages";
            
            log.debug("Sending private message to user {}: {}", userId.id(), message.content());
            
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("Failed to send private message to user {}: {}", userId.id(), e.getMessage(), e);
        }
    }
    
    /**
     * 특정 채팅방에 시스템 메시지 브로드캐스트
     */
    public void broadcastSystemMessage(ChatMessage systemMessage) {
        try {
            String destination = "/topic/chat/" + systemMessage.chatRoomId().id() + "/system";
            
            log.debug("Broadcasting system message to {}: {}", destination, systemMessage.content());
            
            messagingTemplate.convertAndSend(destination, systemMessage);
        } catch (Exception e) {
            log.error("Failed to broadcast system message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 사용자 입장 알림
     */
    public void notifyUserJoined(ChatMessage joinMessage) {
        broadcastSystemMessage(joinMessage);
        
        log.info("User {} joined chat room {}", 
            joinMessage.userId().id(), joinMessage.chatRoomId().id());
    }
    
    /**
     * 사용자 퇴장 알림  
     */
    public void notifyUserLeft(ChatMessage leaveMessage) {
        broadcastSystemMessage(leaveMessage);
        
        log.info("User {} left chat room {}", 
            leaveMessage.userId().id(), leaveMessage.chatRoomId().id());
    }
}