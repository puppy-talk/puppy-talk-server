package com.puppy.talk.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.ChatService;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.chat.command.MessageSendCommand;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * WebSocket 메시지 처리를 위한 비즈니스 로직 서비스
 * 
 * WebSocket 컨트롤러와 도메인 서비스 사이의 중간 계층으로,
 * WebSocket 관련 메시지 변환 및 브로드캐스팅 비즈니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {
    
    private final ChatService chatService;
    private final RealtimeNotificationPort realtimeNotificationPort;
    
    /**
     * 사용자 메시지를 처리하고 실시간으로 브로드캐스트합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @param content 메시지 내용
     */
    @Transactional
    public void processUserMessage(ChatRoomIdentity chatRoomId, UserIdentity userId, String content) {
        log.debug("Processing user message for chatRoom={}, user={}, content='{}'",
            chatRoomId.id(), userId.id(), content);
        
        // 도메인 서비스를 통한 메시지 저장 및 AI 응답 생성
        MessageSendCommand command = MessageSendCommand.of(content);
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, command);
        
        // WebSocket을 통한 실시간 브로드캐스트
        broadcastUserMessage(result, chatRoomId, userId);
    }
    
    /**
     * 타이핑 상태를 처리하고 브로드캐스트합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @param isTyping 타이핑 상태 (true: 타이핑 중, false: 타이핑 종료)
     */
    public void processTypingStatus(ChatRoomIdentity chatRoomId, UserIdentity userId, boolean isTyping) {
        log.debug("Processing typing status for chatRoom={}, user={}, isTyping={}",
            chatRoomId.id(), userId.id(), isTyping);
        
        ChatMessage typingMessage = createTypingMessage(chatRoomId, userId, isTyping);
        realtimeNotificationPort.broadcastTypingStatus(typingMessage);
    }
    
    /**
     * 메시지 읽음 처리를 하고 브로드캐스트합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void processReadReceipt(ChatRoomIdentity chatRoomId, UserIdentity userId) {
        log.debug("Processing read receipt for chatRoom={}, user={}", 
            chatRoomId.id(), userId.id());
        
        // 도메인 서비스를 통한 읽음 처리
        chatService.markMessagesAsRead(chatRoomId);
        
        // WebSocket을 통한 읽음 확인 브로드캐스트
        broadcastReadReceipt(chatRoomId, userId);
    }
    
    // === Private Helper Methods ===
    
    /**
     * 사용자 메시지 브로드캐스트
     */
    private void broadcastUserMessage(MessageSendResult result, ChatRoomIdentity chatRoomId, UserIdentity userId) {
        try {
            ChatMessage webSocketMessage = ChatMessage.newMessage(
                result.message().identity(),
                chatRoomId,
                userId,
                SenderType.USER,
                result.message().content(),
                result.message().isRead()
            );
            
            realtimeNotificationPort.broadcastMessage(webSocketMessage);
            
            log.debug("Successfully broadcasted user message for chatRoom={}, messageId={}", 
                chatRoomId.id(), result.message().identity().id());
                
        } catch (Exception e) {
            log.error("Failed to broadcast user message for chatRoom={}: {}", 
                chatRoomId.id(), e.getMessage(), e);
            throw e; // Re-throw to allow proper error handling at controller level
        }
    }
    
    /**
     * 타이핑 메시지 생성
     */
    private ChatMessage createTypingMessage(ChatRoomIdentity chatRoomId, UserIdentity userId, boolean isTyping) {
        return isTyping 
            ? ChatMessage.typing(chatRoomId, userId, SenderType.USER)
            : ChatMessage.stopTyping(chatRoomId, userId, SenderType.USER);
    }
    
    /**
     * 읽음 확인 브로드캐스트
     * 
     * 실제로는 마지막으로 읽은 메시지 ID를 전달해야 하지만, 
     * 현재 구조상 메시지 ID 없이 읽음 확인을 처리하므로 
     * ChatMessage의 of 메서드를 직접 사용합니다.
     */
    private void broadcastReadReceipt(ChatRoomIdentity chatRoomId, UserIdentity userId) {
        try {
            ChatMessage readReceiptMessage = ChatMessage.of(
                null, // 특정 메시지가 아닌 전체 읽음 처리
                chatRoomId,
                userId,
                SenderType.USER,
                null,
                true,
                com.puppy.talk.websocket.ChatMessageType.READ_RECEIPT
            );
            realtimeNotificationPort.broadcastReadReceipt(readReceiptMessage);
            
            log.debug("Successfully broadcasted read receipt for chatRoom={}, user={}", 
                chatRoomId.id(), userId.id());
                
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt for chatRoom={}, user={}: {}", 
                chatRoomId.id(), userId.id(), e.getMessage(), e);
            throw e; // Re-throw to allow proper error handling at controller level
        }
    }
}