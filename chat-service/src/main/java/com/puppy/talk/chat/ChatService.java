package com.puppy.talk.chat;

import com.puppy.talk.pet.PetNotFoundException;
import com.puppy.talk.ai.AiResponsePort;
import com.puppy.talk.notification.RealtimeNotificationPort;
import com.puppy.talk.pet.PetRepository;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaRepository;
import com.puppy.talk.dto.ChatStartResult;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.chat.command.MessageSendCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.puppy.talk.user.UserIdentity;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int DEFAULT_RECENT_MESSAGE_LIMIT = 50;
    private static final int AI_CONTEXT_MESSAGE_LIMIT = 5;
    private static final String CHAT_ROOM_NAME_PATTERN = "%s와의 채팅방";

    private final PetRepository petRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final AiResponsePort aiResponsePort;
    private final RealtimeNotificationPort realtimeNotificationPort;
    private final PersonaRepository personaRepository;
    private final ActivityTrackingService activityTrackingService;

    /**
     * 펫과의 대화를 시작합니다.
     * 이미 채팅방이 있다면 기존 채팅방을 반환하고, 없다면 새로 생성합니다.
     */
    @Transactional
    public ChatStartResult startChatWithPet(PetIdentity petId) {
        validatePetId(petId);

        Pet pet = findPetOrThrow(petId);
        ChatRoom chatRoom = findOrCreateChatRoom(pet);
        List<Message> recentMessages = findRecentMessages(chatRoom.identity());

        // 채팅방 열기 활동 기록
        trackChatActivity(pet.userId(), chatRoom.identity(), "CHAT_OPENED");

        return new ChatStartResult(chatRoom, pet, recentMessages);
    }

    /**
     * 사용자가 펫에게 메시지를 보냅니다.
     * 사용자 메시지 저장 후 AI 펫 응답을 자동으로 생성하여 저장합니다.
     */
    @Transactional
    public MessageSendResult sendMessageToPet(ChatRoomIdentity chatRoomId, MessageSendCommand command) {
        validateChatRoomId(chatRoomId);
        validateMessageContent(command.content());

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);
        Pet pet = findPetOrThrow(chatRoom.petId());
        
        String trimmedContent = command.content().trim();
        Message savedUserMessage = saveUserMessage(chatRoomId, trimmedContent);
        
        trackChatActivity(pet.userId(), chatRoomId, "MESSAGE_SENT");
        
        // AI 펫 응답 생성 (비동기로 처리 가능)
        generateAndSavePetResponse(chatRoom, pet, trimmedContent);
        
        ChatRoom updatedChatRoom = updateChatRoomTimestamp(chatRoom);
        
        return new MessageSendResult(savedUserMessage, updatedChatRoom);
    }

    /**
     * 채팅방의 모든 메시지를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<Message> getChatHistory(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }

        return messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId);
    }

    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 처리합니다.
     */
    @Transactional
    public void markMessagesAsRead(ChatRoomIdentity chatRoomId) {
        validateChatRoomId(chatRoomId);

        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);
        Pet pet = findPetOrThrow(chatRoom.petId());

        messageRepository.markAllAsReadByChatRoomId(chatRoomId);
        trackChatActivity(pet.userId(), chatRoomId, "MESSAGE_READ");
    }

    /**
     * AI를 사용하여 펫의 응답을 생성하고 저장합니다.
     */
    private void generateAndSavePetResponse(ChatRoom chatRoom, Pet pet, String userMessage) {
        try {
            String aiResponse = generateAiResponse(pet, userMessage, chatRoom.identity());
            Message savedPetMessage = savePetMessage(chatRoom.identity(), aiResponse);
            broadcastPetMessage(chatRoom, pet, savedPetMessage, aiResponse);
            
        } catch (Exception e) {
            // AI 응답 생성 실패 시 로그만 남기고 계속 진행
            log.error("Failed to generate pet response for chatRoom: {}", chatRoom.identity(), e);
        }
    }

    private ChatRoom createNewChatRoom(Pet pet) {
        String roomName = pet.generateChatRoomTitle(); // 도메인 엔티티의 비즈니스 로직 사용
        ChatRoom newChatRoom = ChatRoom.of(
            pet.identity(),
            roomName,
            LocalDateTime.now()
        );
        return chatRoomRepository.save(newChatRoom);
    }

    // === Helper Methods for Improved Readability ===
    
    private void validatePetId(PetIdentity petId) {
        if (petId == null) {
            throw new IllegalArgumentException("PetId cannot be null");
        }
    }
    
    private void validateChatRoomId(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
    }
    
    private void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }
    }
    
    private Pet findPetOrThrow(PetIdentity petId) {
        return petRepository.findByIdentity(petId)
            .orElseThrow(() -> new PetNotFoundException(petId));
    }
    
    private ChatRoom findChatRoomOrThrow(ChatRoomIdentity chatRoomId) {
        return chatRoomRepository.findByIdentity(chatRoomId)
            .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId.id()));
    }
    
    private ChatRoom findOrCreateChatRoom(Pet pet) {
        return chatRoomRepository.findByPetId(pet.identity())
            .orElseGet(() -> createNewChatRoom(pet));
    }
    
    private List<Message> findRecentMessages(ChatRoomIdentity chatRoomId) {
        return messageRepository
            .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
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
        return messageRepository.save(userMessage);
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
        return messageRepository.save(petMessage);
    }
    
    private String generateAiResponse(Pet pet, String userMessage, ChatRoomIdentity chatRoomId) {
        Persona persona = personaRepository.findByIdentity(pet.personaId())
            .orElseThrow(() -> new RuntimeException("Persona not found: " + pet.personaId().id()));
        
        List<Message> chatHistory = messageRepository
            .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId)
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
        ChatRoom updatedChatRoom = chatRoom.updateLastMessageTimeToNow(); // 도메인 엔티티의 비즈니스 로직 사용
        return chatRoomRepository.save(updatedChatRoom);
    }
    
    private void trackChatActivity(UserIdentity userId, ChatRoomIdentity chatRoomId, String activityType) {
        try {
            switch (activityType) {
                case "CHAT_OPENED" -> activityTrackingService.trackChatOpened(userId, chatRoomId);
                case "MESSAGE_SENT" -> activityTrackingService.trackMessageSent(userId, chatRoomId);
                case "MESSAGE_READ" -> activityTrackingService.trackMessageRead(userId, chatRoomId);
                default -> log.warn("Unknown activity type: {}", activityType);
            }
        } catch (Exception e) {
            log.warn("Failed to track activity: {} for user: {}", activityType, userId, e);
        }
    }
}
