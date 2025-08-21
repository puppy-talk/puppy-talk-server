package com.puppy.talk.chat;

import com.puppy.talk.chat.dto.ChatStartResult;
import com.puppy.talk.chat.dto.MessageSendCommand;
import com.puppy.talk.chat.dto.MessageSendResult;
import com.puppy.talk.event.DomainEventPublisher;
import com.puppy.talk.event.MessageSentEvent;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.user.UserIdentity;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ChatFacade implements ChatLookUpService {

    private final ChatDomainService chatDomainService;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 펫과의 채팅을 시작합니다.
     * 도메인 서비스에 모든 비즈니스 로직을 위임합니다.
     * 
     * @param petId 펫 식별자
     * @return 채팅 시작 결과
     */
    public ChatStartResult startChatWithPet(PetIdentity petId) {
        Assert.notNull(petId, "petId cannot be null");
        log.debug("Delegating chat start to domain service for pet: {}", petId.id());

        return chatDomainService.startChatWithPet(petId);
    }

    /**
     * 사용자가 펫에게 메시지를 보냅니다.
     * 도메인 서비스에 모든 비즈니스 로직을 위임합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param command 메시지 전송 명령
     * @return 메시지 전송 결과
     */
    public MessageSendResult sendMessageToPet(ChatRoomIdentity chatRoomId, @Valid MessageSendCommand command) {
        validateChatRoomId(chatRoomId);
        log.debug("Delegating message send to domain service for chatRoom: {}", chatRoomId.id());

        return chatDomainService.sendMessageToPet(chatRoomId, command);
    }

    /**
     * 메시지를 전송하고 도메인 이벤트를 발행합니다.
     * Facade에서 조정하는 복합 유스케이스입니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param command 메시지 전송 명령
     * @param userId 메시지를 보내는 사용자 ID
     * @return 메시지 전송 결과
     */
    public MessageSendResult sendMessageWithEvents(ChatRoomIdentity chatRoomId, @Valid MessageSendCommand command, UserIdentity userId) {
        log.debug("Orchestrating message send with events for chatRoom: {}", chatRoomId.id());
        
        // 1. 도메인 서비스에 메시지 전송 위임
        MessageSendResult result = chatDomainService.sendMessageToPet(chatRoomId, command);
        
        // 2. 애플리케이션 레벨 이벤트 발행 (선택적 부가 기능)
        publishMessageSentEventSafely(result, chatRoomId, userId, command);
        
        return result;
    }


    /**
     * 채팅 히스토리를 조회합니다.
     * 도메인 서비스에 조회 로직을 위임합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @return 메시지 목록
     */
    public List<Message> getChatHistory(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        log.debug("Delegating chat history retrieval to domain service for chatRoom: {}", chatRoomId.id());
        
        return chatDomainService.getChatRoomMessageHistory(chatRoomId, Integer.MAX_VALUE);
    }

    /**
     * 메시지를 읽음 상태로 변경합니다.
     * 도메인 서비스에 비즈니스 로직을 위임합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param userId 사용자 식별자
     */
    public void markMessagesAsRead(ChatRoomIdentity chatRoomId, UserIdentity userId) {
        validateChatRoomId(chatRoomId);
        Assert.notNull(userId, "userId cannot be null");
        log.debug("Delegating mark messages as read to domain service for chatRoom: {}", chatRoomId.id());

        chatDomainService.markAllMessagesAsRead(chatRoomId, userId);
    }

    /**
     * 메시지를 읽음 상태로 변경합니다 (편의 메서드).
     * 채팅방 정보에서 사용자 정보를 자동으로 조회합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     */
    public void markMessagesAsRead(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        log.debug("Looking up user from chatRoom and delegating mark messages as read to domain service for chatRoom: {}", chatRoomId.id());

        // 채팅방 정보에서 사용자 정보를 조회하여 위임
        UserIdentity userId = getUserIdFromChatRoom(chatRoomId);
        chatDomainService.markAllMessagesAsRead(chatRoomId, userId);
    }

    private UserIdentity getUserIdFromChatRoom(ChatRoomIdentity chatRoomId) {
        // 도메인 서비스를 통해 채팅방 → 펫 → 사용자 순으로 조회
        return chatDomainService.getUserIdFromChatRoom(chatRoomId);
    }

    // === Facade Helper Methods ===
    
    private void validateChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
    }
    
    /**
     * 애플리케이션 레벨 이벤트 발행을 안전하게 처리합니다.
     * 이벤트 발행 실패가 비즈니스 로직에 영향을 주지 않도록 합니다.
     */
    private void publishMessageSentEventSafely(MessageSendResult result, ChatRoomIdentity chatRoomId, 
                                               UserIdentity userId, MessageSendCommand command) {
        try {
            MessageSentEvent event = MessageSentEvent.of(
                result.message().identity(),
                chatRoomId,
                userId,
                SenderType.USER,
                command.content(),
                result.message().isRead()
            );
            
            domainEventPublisher.publish(event);
            
            log.debug("Successfully published MessageSentEvent for user: {} in chatRoom: {}", 
                userId.id(), chatRoomId.id());
            
        } catch (Exception e) {
            log.warn("Failed to publish message sent event for chatRoom: {}", chatRoomId.id(), e);
            // 이벤트 발행 실패가 메시지 전송을 실패시키지 않도록 함
        }
    }
    
    // === ChatLookUpService Interface Implementation ===
    
    @Override
    public ChatStartResult startChat(UserIdentity userId, PetIdentity petId) {
        if (userId == null || petId == null) {
            throw new IllegalArgumentException("UserId and PetId cannot be null");
        }
        
        // Delegate to domain service
        return chatDomainService.startChatWithPet(petId);
    }
    
    @Override
    public MessageSendResult sendMessage(MessageSendCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("MessageSendCommand cannot be null");
        }
        
        // Since MessageSendCommand doesn't contain chatRoomId, we need to implement this differently
        // This is a simplified implementation for interface compliance
        throw new UnsupportedOperationException("sendMessage with only MessageSendCommand is not implemented. " +
            "Use sendMessageToPet(ChatRoomIdentity, MessageSendCommand) instead.");
    }
    
    @Override
    public List<ChatRoom> getChatRooms(UserIdentity userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        // Delegate to domain service
        return chatDomainService.getUserChatRooms(userId);
    }
    
    @Override
    public List<Message> getMessageHistory(ChatRoomIdentity chatRoomId, int limit) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        
        // Delegate to domain service
        return chatDomainService.getChatRoomMessageHistory(chatRoomId, limit);
    }
    
    @Override
    public void markAllMessagesAsRead(ChatRoomIdentity chatRoomId, UserIdentity userId) {
        if (chatRoomId == null || userId == null) {
            throw new IllegalArgumentException("ChatRoomId and UserId cannot be null");
        }
        
        // Delegate to domain service
        chatDomainService.markAllMessagesAsRead(chatRoomId, userId);
    }
}