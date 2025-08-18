package com.puppy.talk.domain.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.domain.websocket.dto.ChatMessageRequest;
import com.puppy.talk.domain.websocket.dto.ReadReceiptRequest;
import com.puppy.talk.domain.websocket.dto.TypingRequest;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.WebSocketMessageService;
import jakarta.validation.Valid;
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
    
    private final WebSocketMessageService webSocketMessageService;
    
    /**
     * 채팅방에 메시지 전송
     * /app/chat/{chatRoomId}/send
     */
    @MessageMapping("/chat/{chatRoomId}/send")
    public void sendMessage(
        @DestinationVariable Long chatRoomId,
        @Payload @Valid ChatMessageRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        ChatRoomIdentity roomId = ChatRoomIdentity.of(chatRoomId);
        UserIdentity userId = UserIdentity.of(request.userId());
        
        log.debug("Received message for chatRoom={}, user={}, content='{}'",
            chatRoomId, request.userId(), request.content());
        
        webSocketMessageService.processUserMessage(roomId, userId, request.content());
    }
    
    /**
     * 타이핑 상태 알림
     * /app/chat/{chatRoomId}/typing
     */
    @MessageMapping("/chat/{chatRoomId}/typing")
    public void handleTyping(
        @DestinationVariable Long chatRoomId,
        @Payload @Valid TypingRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        ChatRoomIdentity roomId = ChatRoomIdentity.of(chatRoomId);
        UserIdentity userId = UserIdentity.of(request.userId());
        
        webSocketMessageService.processTypingStatus(roomId, userId, request.isTyping());
    }
    
    /**
     * 메시지 읽음 처리
     * /app/chat/{chatRoomId}/read
     */
    @MessageMapping("/chat/{chatRoomId}/read")
    public void markMessagesAsRead(
        @DestinationVariable Long chatRoomId,
        @Payload @Valid ReadReceiptRequest request,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        ChatRoomIdentity roomId = ChatRoomIdentity.of(chatRoomId);
        UserIdentity userId = UserIdentity.of(request.userId());
        
        webSocketMessageService.processReadReceipt(roomId, userId);
    }
    
}