package com.puppy.talk.chat;

import com.puppy.talk.activity.ActivityTrackingService;
import com.puppy.talk.chat.service.AiResponseService;
import com.puppy.talk.chat.dto.ChatStartResult;
import com.puppy.talk.chat.dto.MessageSendCommand;
import com.puppy.talk.chat.dto.MessageSendResult;
import com.puppy.talk.chat.service.ChatRoomLookUpService;
import com.puppy.talk.chat.service.MessageLookUpService;
import com.puppy.talk.event.DomainEventPublisher;
import com.puppy.talk.event.MessageSentEvent;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.service.PetLookUpService;
import com.puppy.talk.pet.service.PersonaLookUpService;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.websocket.ChatMessageBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 도메인 서비스
 * 
 * 채팅과 관련된 핵심 비즈니스 로직을 담당합니다.
 * 도메인 객체들을 조합하여 복잡한 비즈니스 규칙을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatDomainService {

    private static final int AI_CONTEXT_MESSAGE_LIMIT = 5;
    private static final String ACTIVITY_CHAT_OPENED = "CHAT_OPENED";
    private static final String ACTIVITY_MESSAGE_SENT = "MESSAGE_SENT";
    private static final String ACTIVITY_MESSAGE_READ = "MESSAGE_READ";

    private final PetLookUpService petLookUpService;
    private final PersonaLookUpService personaLookUpService;
    private final ChatRoomLookUpService chatRoomLookUpService;
    private final MessageLookUpService messageLookUpService;
    private final ActivityTrackingService activityTrackingService;
    private final AiResponseService aiResponseService;
    private final ChatMessageBroadcaster chatMessageBroadcaster;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 펫과의 채팅을 시작하는 비즈니스 로직을 처리합니다.
     */
    @Transactional
    public ChatStartResult startChatWithPet(PetIdentity petId) {
        // 1. 펫 정보 조회
        Pet pet = petLookUpService.findPet(petId);
        
        // 2. 채팅방 찾기 또는 생성
        ChatRoom chatRoom = findOrCreateChatRoom(pet);
        
        // 3. 최근 메시지 조회
        List<Message> recentMessages = findRecentMessages(chatRoom.identity());
        
        // 4. 활동 추적
        trackChatActivity(pet.userId(), chatRoom.identity(), ACTIVITY_CHAT_OPENED);
        
        // 5. 결과 반환 (Pet 정보도 필요하므로 조회)
        return new ChatStartResult(chatRoom, pet, recentMessages);
    }

    /**
     * 메시지 전송 비즈니스 로직을 처리합니다.
     */
    @Transactional
    public MessageSendResult sendMessageToPet(ChatRoomIdentity chatRoomId, MessageSendCommand command) {
        // 1. 채팅방 정보 조회
        ChatRoom chatRoom = chatRoomLookUpService.findChatRoom(chatRoomId);
        Pet pet = petLookUpService.findPet(chatRoom.petId());

        // 2. 사용자 메시지 저장
        Message savedUserMessage = saveUserMessage(chatRoomId, command.content());
        
        // 3. 채팅방 업데이트
        ChatRoom updatedChatRoom = updateChatRoomTimestamp(chatRoom);
        
        // 4. 활동 추적
        trackChatActivity(pet.userId(), chatRoomId, ACTIVITY_MESSAGE_SENT);
        
        // 5. 도메인 이벤트 발행
        publishMessageSentEvent(savedUserMessage, pet.userId());
        
        // 6. AI 응답 생성 (비동기적으로 처리)
        generateAndSavePetResponse(updatedChatRoom, pet, command.content());
        
        return new MessageSendResult(savedUserMessage, updatedChatRoom);
    }

    /**
     * 채팅방의 모든 메시지를 읽음 처리하는 비즈니스 로직을 처리합니다.
     */
    @Transactional
    public void markAllMessagesAsRead(ChatRoomIdentity chatRoomId, UserIdentity userId) {
        // 1. 모든 메시지를 읽음 처리
        messageLookUpService.markAllMessagesAsReadByChatRoomId(chatRoomId);
        
        // 2. 활동 추적
        trackChatActivity(userId, chatRoomId, ACTIVITY_MESSAGE_READ);
        
        log.debug("Marked all messages as read for chatRoom: {}, user: {}", 
            chatRoomId.id(), userId.id());
    }

    /**
     * 사용자의 채팅방 목록을 조회하는 비즈니스 로직을 처리합니다.
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getUserChatRooms(UserIdentity userId) {
        // TODO: 실제 구현에서는 사용자의 채팅방 목록을 조회
        // 현재는 placeholder 구현
        log.debug("Getting chat rooms for user: {}", userId.id());
        return List.of();
    }

    /**
     * 채팅방의 메시지 히스토리를 조회하는 비즈니스 로직을 처리합니다.
     */
    @Transactional(readOnly = true)
    public List<Message> getChatRoomMessageHistory(ChatRoomIdentity chatRoomId, int limit) {
        return messageLookUpService
            .findMessagesByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
            .stream()
            .limit(limit)
            .toList();
    }

    /**
     * 채팅방에서 사용자 ID를 조회하는 비즈니스 로직을 처리합니다.
     */
    @Transactional(readOnly = true)
    public UserIdentity getUserIdFromChatRoom(ChatRoomIdentity chatRoomId) {
        ChatRoom chatRoom = chatRoomLookUpService.findChatRoom(chatRoomId);
        Pet pet = petLookUpService.findPet(chatRoom.petId());
        return pet.userId();
    }

    // === Private Helper Methods for Business Logic ===

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
            .limit(50) // DEFAULT_RECENT_MESSAGE_LIMIT
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

    private ChatRoom updateChatRoomTimestamp(ChatRoom chatRoom) {
        ChatRoom updatedChatRoom = chatRoom.updateLastMessageTimeToNow();
        return chatRoomLookUpService.createChatRoom(updatedChatRoom); // save 대신 create 사용
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
        Persona persona = personaLookUpService.findPersona(pet.personaId());
        
        List<Message> chatHistory = messageLookUpService
            .findMessagesByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
            .stream()
            .limit(AI_CONTEXT_MESSAGE_LIMIT)
            .toList();
        
        return aiResponseService.generatePetResponse(pet, persona, userMessage, chatHistory);
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
            chatMessageBroadcaster.broadcastMessage(webSocketMessage);
        } catch (Exception e) {
            log.warn("Failed to broadcast pet message for chatRoom: {}", chatRoom.identity(), e);
        }
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

    private void publishMessageSentEvent(Message message, UserIdentity userId) {
        try {
            MessageSentEvent event = MessageSentEvent.of(
                message.identity(),
                message.chatRoomId(),
                userId,
                message.senderType(),
                message.content(),
                message.isRead()
            );
            domainEventPublisher.publish(event);
            log.debug("Published MessageSentEvent for message: {}", message.identity().id());
        } catch (Exception e) {
            log.warn("Failed to publish MessageSentEvent for message: {}", message.identity().id(), e);
        }
    }
}