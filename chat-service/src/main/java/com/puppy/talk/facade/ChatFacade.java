package com.puppy.talk.facade;

import com.puppy.talk.chat.ChatService;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.ChatRoomNotFoundException;
import com.puppy.talk.chat.ActivityTrackingService;
import com.puppy.talk.chat.MessageRepository;
import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.pet.PetRepository;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.pet.PersonaRepository;
import com.puppy.talk.dto.ChatStartResult;
import com.puppy.talk.dto.MessageSendResult;
import com.puppy.talk.chat.command.MessageSendCommand;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.event.DomainEventPublisher;
import com.puppy.talk.event.MessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅 관련 서비스들을 조정하는 Facade 클래스
 * 
 * Facade 패턴을 적용하여 ChatService, PersonaLookUpService, ActivityTrackingService 간의
 * 복잡한 상호작용을 단순화하고, 같은 계층 내 의존성 문제를 해결합니다.
 * 
 * Layered Architecture에서 Business Logic Layer 내의 서비스 간 조정을 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final ChatService chatService;
    private final ActivityTrackingService activityTrackingService;
    private final DomainEventPublisher domainEventPublisher;
    private final PetRepository petRepository;
    private final PersonaRepository personaRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    /**
     * 펫과의 채팅을 시작하면서 페르소나 정보도 함께 로드합니다.
     * 
     * @param petId 펫 식별자
     * @return 채팅 시작 결과와 페르소나 정보를 포함한 통합 결과
     */
    @Transactional
    public EnrichedChatStartResult startChatWithPersona(PetIdentity petId) {
        log.debug("Starting chat with persona for pet: {}", petId.id());
        
        // 1. 기본 채팅 시작
        ChatStartResult chatResult = chatService.startChatWithPet(petId);
        
        // 2. 페르소나 정보 로드 (infrastructure port 사용)
        Persona persona = personaRepository.findByIdentity(chatResult.pet().personaId())
            .orElseThrow(() -> new RuntimeException("Persona not found: " + chatResult.pet().personaId().id()));
        
        // 3. 통합 결과 반환
        return new EnrichedChatStartResult(
            chatResult.chatRoom(),
            chatResult.pet(),
            persona,
            chatResult.recentMessages()
        );
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
    public MessageSendResult sendMessageWithEvents(ChatRoomIdentity chatRoomId, MessageSendCommand command, UserIdentity userId) {
        log.debug("Sending message with domain events for chatRoom: {}", chatRoomId.id());
        
        // 1. 메시지 전송 (ChatService에 위임)
        MessageSendResult result = chatService.sendMessageToPet(chatRoomId, command);
        
        // 2. 도메인 이벤트 발행 - 결합도 낮추기 (현재는 예시로만 구현)
        try {
            MessageSentEvent event = MessageSentEvent.of(
                result.message().identity(),
                chatRoomId,
                userId,
                com.puppy.talk.chat.SenderType.USER,
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
     * 펫과의 채팅을 시작합니다 (기본 버전).
     * 
     * @param petId 펫 식별자
     * @return 채팅 시작 결과
     */
    @Transactional
    public ChatStartResult startChatWithPet(PetIdentity petId) {
        log.debug("Starting chat with pet: {}", petId.id());
        return chatService.startChatWithPet(petId);
    }

    /**
     * 메시지를 전송합니다 (기본 버전).
     * 
     * @param chatRoomId 채팅방 식별자
     * @param command 메시지 전송 명령
     * @return 메시지 전송 결과
     */
    @Transactional
    public MessageSendResult sendMessageToPet(ChatRoomIdentity chatRoomId, MessageSendCommand command) {
        log.debug("Sending message to pet for chatRoom: {}", chatRoomId.id());
        return chatService.sendMessageToPet(chatRoomId, command);
    }

    /**
     * 채팅 히스토리를 조회합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @return 메시지 목록
     */
    @Transactional(readOnly = true)
    public java.util.List<com.puppy.talk.chat.Message> getChatHistory(ChatRoomIdentity chatRoomId) {
        log.debug("Getting chat history for chatRoom: {}", chatRoomId.id());
        return chatService.getChatHistory(chatRoomId);
    }

    /**
     * 메시지를 읽음 상태로 변경합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     */
    @Transactional
    public void markMessagesAsRead(ChatRoomIdentity chatRoomId) {
        log.debug("Marking messages as read for chatRoom: {}", chatRoomId.id());
        chatService.markMessagesAsRead(chatRoomId);
    }

    /**
     * 특정 사용자의 모든 채팅방을 페르소나 정보와 함께 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 페르소나 정보가 포함된 채팅방 목록
     */
    @Transactional(readOnly = true)
    public java.util.List<EnrichedChatRoomInfo> getUserChatRoomsWithPersona(UserIdentity userId) {
        log.debug("Getting chat rooms with persona for user: {}", userId.id());
        
        // 사용자의 모든 펫 조회
        java.util.List<Pet> userPets = petRepository.findByUserId(userId);
        
        return userPets.stream()
            .map(pet -> {
                // 각 펫의 채팅방 조회
                ChatRoom chatRoom = chatRoomRepository.findByPetId(pet.identity())
                    .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다: " + pet.identity().id()));
                
                // 페르소나 조회
                Persona persona = personaRepository.findByIdentity(pet.personaId())
                    .orElseThrow(() -> new RuntimeException("Persona not found: " + pet.personaId().id()));
                
                // 읽지 않은 메시지 개수 계산 (간단한 구현)
                int unreadCount = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoom.identity()).size();
                
                // 마지막 활동 시간 (간단한 구현)
                java.time.LocalDateTime lastActivity = java.time.LocalDateTime.now();
                
                return new EnrichedChatRoomInfo(chatRoom, pet, persona, unreadCount, lastActivity);
            })
            .collect(java.util.stream.Collectors.toList());
    }

}