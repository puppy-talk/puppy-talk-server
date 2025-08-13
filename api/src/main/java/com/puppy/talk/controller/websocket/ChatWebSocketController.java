package com.puppy.talk.controller.websocket;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.user.UserIdentity;
import com.puppy.talk.model.websocket.ChatMessage;
import com.puppy.talk.model.websocket.ChatMessageType;
import com.puppy.talk.service.chat.ChatService;
import com.puppy.talk.service.websocket.WebSocketChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket 채팅 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    
    private final WebSocketChatService webSocketChatService;
    private final ChatService chatService;
    
    /**
     * 채팅방에 메시지 전송
     * /app/chat/{chatRoomId}/send
     */
    @MessageMapping("/chat/{chatRoomId}/send")
    public void sendMessage(
        @DestinationVariable Long chatRoomId,
        @Payload ChatMessageRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            ChatRoomIdentity roomId = ChatRoomIdentity.of(chatRoomId);
            UserIdentity userId = UserIdentity.of(request.userId());
            
            log.debug("Received message for chatRoom={}, user={}, content='{}'", 
                chatRoomId, request.userId(), request.content());
            
            // 메시지를 데이터베이스에 저장하고 AI 응답 생성
            var result = chatService.sendMessageToPet(roomId, request.content());
            
            // WebSocket을 통해 실시간으로 브로드캐스트
            webSocketChatService.broadcastMessage(
                ChatMessage.newMessage(
                    result.message().identity(),
                    roomId,
                    userId,
                    SenderType.USER,
                    result.message().content(),
                    result.message().isRead()
                )
            );
            
            // AI 응답도 즉시 브로드캐스트 (ChatService에서 이미 생성됨)
            // 추가적인 AI 응답 처리는 ChatService에서 WebSocketChatService를 호출하도록 수정 필요
            
        } catch (Exception e) {
            log.error("Error sending message to chatRoom={}: {}", chatRoomId, e.getMessage(), e);
            // 에러 메시지를 해당 사용자에게만 전송
            webSocketChatService.sendToUser(
                UserIdentity.of(request.userId()),
                ChatMessage.of(
                    null,
                    ChatRoomIdentity.of(chatRoomId),
                    UserIdentity.of(request.userId()),
                    SenderType.SYSTEM,
                    "메시지 전송 중 오류가 발생했습니다: " + e.getMessage(),
                    false,
                    ChatMessageType.SYSTEM
                )
            );
        }
    }
    
    /**
     * 타이핑 상태 알림
     * /app/chat/{chatRoomId}/typing
     */
    @MessageMapping("/chat/{chatRoomId}/typing")
    public void handleTyping(
        @DestinationVariable Long chatRoomId,
        @Payload TypingRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            ChatRoomIdentity roomId = ChatRoomIdentity.of(chatRoomId);
            UserIdentity userId = UserIdentity.of(request.userId());
            
            ChatMessage typingMessage = request.isTyping() 
                ? ChatMessage.typing(roomId, userId, SenderType.USER)
                : ChatMessage.stopTyping(roomId, userId, SenderType.USER);
                
            webSocketChatService.broadcastTyping(typingMessage);
            
        } catch (Exception e) {
            log.error("Error handling typing for chatRoom={}: {}", chatRoomId, e.getMessage(), e);
        }
    }
    
    /**
     * 메시지 읽음 처리
     * /app/chat/{chatRoomId}/read
     */
    @MessageMapping("/chat/{chatRoomId}/read")
    public void markMessagesAsRead(
        @DestinationVariable Long chatRoomId,
        @Payload ReadReceiptRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        try {
            ChatRoomIdentity roomId = ChatRoomIdentity.of(chatRoomId);
            UserIdentity userId = UserIdentity.of(request.userId());
            
            // 데이터베이스에서 읽음 처리
            chatService.markMessagesAsRead(roomId);
            
            // 실시간으로 읽음 상태 브로드캐스트
            webSocketChatService.broadcastReadReceipt(
                ChatMessage.readReceipt(roomId, userId, null)
            );
            
        } catch (Exception e) {
            log.error("Error marking messages as read for chatRoom={}: {}", chatRoomId, e.getMessage(), e);
        }
    }
    
    /**
     * 채팅 메시지 요청 DTO
     */
    public record ChatMessageRequest(
        Long userId,
        String content
    ) {}
    
    /**
     * 타이핑 상태 요청 DTO
     */
    public record TypingRequest(
        Long userId,
        boolean isTyping
    ) {}
    
    /**
     * 읽음 확인 요청 DTO
     */
    public record ReadReceiptRequest(
        Long userId,
        Long lastReadMessageId
    ) {}
}