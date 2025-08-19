package com.puppy.talk.chat;

import com.puppy.talk.activity.service.ActivityTrackingService;
import com.puppy.talk.ai.AiResponsePort;
import com.puppy.talk.chat.dto.ChatStartResult;
import com.puppy.talk.chat.dto.MessageSendResult;
import com.puppy.talk.chat.dto.MessageSendCommand;
import com.puppy.talk.chat.service.ChatRoomLookUpService;
import com.puppy.talk.chat.service.MessageLookUpService;
import com.puppy.talk.event.DomainEventPublisher;
import com.puppy.talk.event.MessageSentEvent;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.PetNotFoundException;
import com.puppy.talk.pet.PetRepository;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaNotFoundException;
import com.puppy.talk.pet.PersonaRepository;
import com.puppy.talk.chat.ChatRoomNotFoundException;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.Assert;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 채팅 도메인의 비즈니스 로직을 처리하는 Facade 클래스
 * 
 * Facade 패턴을 적용하여 복잡한 채팅 비즈니스 로직을 캡슐화하고,
 * Repository, LookUpService, 외부 Port들을 조정합니다.
 * 
 * 📋 주요 책임:
 * ✅ 채팅방 생성 및 관리
 * ✅ 메시지 송수신 처리
 * ✅ AI 응답 생성 및 브로드캐스트
 * ✅ 활동 추적 및 이벤트 발행
 */
@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class ChatFacade {

    private static final int DEFAULT_RECENT_MESSAGE_LIMIT = 50;
    private static final int AI_CONTEXT_MESSAGE_LIMIT = 5;
    
    // Activity type constants
    private static final String ACTIVITY_CHAT_OPENED = "CHAT_OPENED";
    private static final String ACTIVITY_MESSAGE_SENT = "MESSAGE_SENT";
    private static final String ACTIVITY_MESSAGE_READ = "MESSAGE_READ";

    // Repository 직접 사용 (Facade 패턴)
    private final PetRepository petRepository;
    private final PersonaRepository personaRepository;
    
    // LookUpService 사용
    private final ChatRoomLookUpService chatRoomLookUpService;
    private final MessageLookUpService messageLookUpService;
    
    // 외부 Port 및 다른 도메인 Service
    private final ActivityTrackingService activityTrackingService;
    private final AiResponsePort aiResponsePort;
    private final RealtimeNotificationPort realtimeNotificationPort;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 펫과의 채팅을 시작합니다.
     * 이미 채팅방이 있다면 기존 채팅방을 반환하고, 없다면 새로 생성합니다.
     * 
     * @param petId 펫 식별자
     * @return 채팅 시작 결과
     */
    @Transactional
    public ChatStartResult startChatWithPet(PetIdentity petId) {
        Assert.notNull(petId, "petId cannot be null");
        log.debug("Starting chat for pet: {}", petId.id());

        Pet pet = findPetOrThrow(petId);
        ChatRoom chatRoom = findOrCreateChatRoom(pet);
        List<Message> recentMessages = findRecentMessages(chatRoom.identity());

        // 채팅방 열기 활동 기록
        trackChatActivity(pet.userId(), chatRoom.identity(), ACTIVITY_CHAT_OPENED);

        return new ChatStartResult(chatRoom, pet, recentMessages);
    }

    /**
     * 사용자가 펫에게 메시지를 보냅니다.
     * 사용자 메시지 저장 후 AI 펫 응답을 자동으로 생성하여 저장합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param command 메시지 전송 명령
     * @return 메시지 전송 결과
     */
    @Transactional
    public MessageSendResult sendMessageToPet(ChatRoomIdentity chatRoomId, @Valid MessageSendCommand command) {
        validateChatRoomId(chatRoomId);
        log.debug("Sending message to pet for chatRoom: {}", chatRoomId.id());

        ChatRoom chatRoom = chatRoomLookUpService.findChatRoom(chatRoomId);
        Pet pet = findPetOrThrow(chatRoom.petId());
        
        String trimmedContent = command.content().trim();
        Message savedUserMessage = saveUserMessage(chatRoomId, trimmedContent);
        
        trackChatActivity(pet.userId(), chatRoomId, ACTIVITY_MESSAGE_SENT);
        
        // AI 펫 응답 생성 (비동기로 처리하여 트랜잭션 범위 최적화)
        // TODO: 향후 @Async를 사용하여 비동기 처리로 변경 고려
        generateAndSavePetResponse(chatRoom, pet, trimmedContent);
        
        ChatRoom updatedChatRoom = updateChatRoomTimestamp(chatRoom);
        
        return new MessageSendResult(savedUserMessage, updatedChatRoom);
    }

    /**
     * 메시지를 전송하고 도메인 이벤트를 발행합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param command 메시지 전송 명령
     * @param userId 메시지를 보내는 사용자 ID
     * @return 메시지 전송 결과
     */
    @Transactional
    public MessageSendResult sendMessageWithEvents(ChatRoomIdentity chatRoomId, @Valid MessageSendCommand command, UserIdentity userId) {
        log.debug("Sending message with domain events for chatRoom: {}", chatRoomId.id());
        
        // 1. 메시지 전송
        MessageSendResult result = sendMessageToPet(chatRoomId, command);
        
        // 2. 도메인 이벤트 발행
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
        
        return result;
    }


    /**
     * 채팅 히스토리를 조회합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @return 메시지 목록
     */
    @Transactional(readOnly = true)
    public List<Message> getChatHistory(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        log.debug("Getting chat history for chatRoom: {}", chatRoomId.id());
        
        return messageLookUpService.findMessagesByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }

    /**
     * 메시지를 읽음 상태로 변경합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     */
    @Transactional
    public void markMessagesAsRead(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);
        log.debug("Marking messages as read for chatRoom: {}", chatRoomId.id());

        ChatRoom chatRoom = chatRoomLookUpService.findChatRoom(chatRoomId);
        Pet pet = findPetOrThrow(chatRoom.petId());

        messageLookUpService.markAllMessagesAsReadByChatRoomId(chatRoomId);
        trackChatActivity(pet.userId(), chatRoomId, ACTIVITY_MESSAGE_READ);
    }

    // === Helper Methods ===
    
    private void validateChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
    }
    
    private Pet findPetOrThrow(PetIdentity petId) {
        return petRepository.findByIdentity(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));
    }
    
    private ChatRoom findOrCreateChatRoom(Pet pet) {
        try {
            return chatRoomLookUpService.findChatRoomByPetId(pet.identity());
        } catch (ChatRoomNotFoundException e) {
            log.debug("ChatRoom not found for pet: {}, creating new chatRoom", pet.identity().id());
            return createNewChatRoom(pet);
        }
    }
    
    private ChatRoom createNewChatRoom(Pet pet) {
        String roomName = pet.generateChatRoomTitle();
        ChatRoom newChatRoom = ChatRoom.of(
            pet.identity(),
            roomName,
            LocalDateTime.now()
        );
        return chatRoomLookUpService.createChatRoom(newChatRoom);
    }
    
    private List<Message> findRecentMessages(ChatRoomIdentity chatRoomId) {
        return messageLookUpService
            .findMessagesByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
            .stream()
            .limit(DEFAULT_RECENT_MESSAGE_LIMIT)
            .toList();
    }
    
    private Message saveUserMessage(ChatRoomIdentity chatRoomId, String content) {
        Message userMessage = Message.of(
            null, // identity는 저장 시 생성됨
            chatRoomId,
            SenderType.USER,
            content,
            true, // 사용자 메시지는 항상 읽음 처리
            LocalDateTime.now()
        );
        return messageLookUpService.sendMessage(userMessage);
    }
    
    private Message savePetMessage(ChatRoomIdentity chatRoomId, String content) {
        Message petMessage = Message.of(
            null, // identity는 저장 시 생성됨
            chatRoomId,
            SenderType.PET,
            content,
            false, // 펫 메시지는 처음에 읽지 않음 상태
            LocalDateTime.now()
        );
        return messageLookUpService.sendMessage(petMessage);
    }
    
    private void generateAndSavePetResponse(ChatRoom chatRoom, Pet pet, String userMessage) {
        try {
            String aiResponse = generateAiResponse(pet, userMessage, chatRoom.identity());
            Message savedPetMessage = savePetMessage(chatRoom.identity(), aiResponse);
            broadcastPetMessage(chatRoom, pet, savedPetMessage, aiResponse);
            
        } catch (Exception e) {
            log.error("Failed to generate pet response for chatRoom: {}", chatRoom.identity(), e);
        }
    }
    
    private String generateAiResponse(Pet pet, String userMessage, ChatRoomIdentity chatRoomId) {
        Persona persona = personaRepository.findByIdentity(pet.personaId())
            .orElseThrow(() -> new PersonaNotFoundException(pet.personaId()));
        
        List<Message> chatHistory = messageLookUpService
            .findMessagesByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
            .stream()
            .limit(AI_CONTEXT_MESSAGE_LIMIT)
            .toList();
        
        return aiResponsePort.generatePetResponse(pet, persona, userMessage, chatHistory);
    }
    
    private void broadcastPetMessage(ChatRoom chatRoom, Pet pet, Message savedPetMessage, String content) {
        try {
            ChatMessage webSocketMessage = ChatMessage.newMessage(
                savedPetMessage.identity(),
                chatRoom.identity(),
                pet.userId(),
                SenderType.PET,
                content,
                false
            );
            realtimeNotificationPort.broadcastMessage(webSocketMessage);
        } catch (Exception e) {
            log.warn("Failed to broadcast pet message for chatRoom: {}", chatRoom.identity(), e);
        }
    }
    
    private ChatRoom updateChatRoomTimestamp(ChatRoom chatRoom) {
        ChatRoom updatedChatRoom = chatRoom.updateLastMessageTimeToNow();
        return chatRoomLookUpService.createChatRoom(updatedChatRoom); // save 대신 create 사용
    }
    
    private void trackChatActivity(UserIdentity userId, ChatRoomIdentity chatRoomId, String activityType) {
        try {
            switch (activityType) {
                case ACTIVITY_CHAT_OPENED -> activityTrackingService.trackChatOpened(userId, chatRoomId);
                case ACTIVITY_MESSAGE_SENT -> activityTrackingService.trackMessageSent(userId, chatRoomId);
                case ACTIVITY_MESSAGE_READ -> activityTrackingService.trackMessageRead(userId, chatRoomId);
                default -> log.warn("Unknown activity type: {}", activityType);
            }
        } catch (Exception e) {
            log.warn("Failed to track activity: {} for user: {}", activityType, userId, e);
        }
    }
}