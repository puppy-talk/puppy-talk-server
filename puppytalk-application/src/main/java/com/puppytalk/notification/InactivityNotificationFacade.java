package com.puppytalk.notification;

import com.puppytalk.chat.AiMessageGenerationService;
import com.puppytalk.chat.ChatDomainService;
import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserId;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비활성 사용자 알림 파사드
 * 
 * Application 레이어: AI 기반 개인화 알림 생성 오케스트레이션
 */
@Service
@Transactional(readOnly = true)
public class InactivityNotificationFacade {
    
    private static final Logger log = LoggerFactory.getLogger(InactivityNotificationFacade.class);
    private static final int CHAT_HISTORY_LIMIT = 10;
    
    private final NotificationDomainService notificationDomainService;
    private final ChatDomainService chatDomainService;
    private final PetRepository petRepository;
    private final AiMessageGenerationService aiMessageGenerationService;
    
    public InactivityNotificationFacade(
        NotificationDomainService notificationDomainService,
        ChatDomainService chatDomainService,
        PetRepository petRepository,
        AiMessageGenerationService aiMessageGenerationService
    ) {
        this.notificationDomainService = notificationDomainService;
        this.chatDomainService = chatDomainService;
        this.petRepository = petRepository;
        this.aiMessageGenerationService = aiMessageGenerationService;
    }
    
    /**
     * 비활성 사용자에게 AI 기반 개인화 알림 생성 (Primitive 타입 사용)
     * 
     * @param userId 비활성 사용자 ID (Long)
     * @param petId 반려동물 ID (Long)
     * @return 생성된 알림 ID (실패 시 null)
     */
    @Transactional
    public Long createInactivityNotification(Long userId, Long petId) {
        if (userId == null || petId == null) {
            log.warn("Invalid parameters: userId={}, petId={}", userId, petId);
            return null;
        }
        
        Optional<NotificationId> result = createInactivityNotificationInternal(
            UserId.from(userId), 
            PetId.from(petId)
        );
        
        return result.map(NotificationId::getValue).orElse(null);
    }
    
    /**
     * 내부 구현: 도메인 타입을 사용한 실제 로직
     */
    private Optional<NotificationId> createInactivityNotificationInternal(UserId userId, PetId petId) {
        log.debug("Creating inactivity notification for user: {}, pet: {}", userId.getValue(), petId.getValue());
        
        // 1. 반려동물 정보 조회
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> {
                log.warn("Pet not found: {}", petId.getValue());
                return new IllegalArgumentException("Pet not found: " + petId);
            });
        log.debug("Found pet: {} with persona: {}", pet.name(), pet.persona());
        
        // 2. 채팅방 조회
        Optional<ChatRoom> chatRoom = chatDomainService.findChatRoomByUserAndPet(userId, petId);
        if (chatRoom.isEmpty()) {
            log.info("No chat room found for user: {} and pet: {}, skipping notification", 
                    userId.getValue(), petId.getValue());
            return Optional.empty();
        }
        
        ChatRoomId chatRoomId = chatRoom.get().id();
        log.debug("Found chat room: {}", chatRoomId.getValue());
        
        // 3. 최근 채팅 히스토리 조회
        List<Message> chatHistory = chatDomainService.findRecentChatHistory(chatRoomId, CHAT_HISTORY_LIMIT);
        log.debug("Retrieved {} recent messages for context", chatHistory.size());
        
        // 4. AI 메시지 생성
        log.debug("Generating AI message for pet persona: {}", pet.persona());
        AiMessageGenerationService.AiMessageResult aiResult = 
            aiMessageGenerationService.generateInactivityMessage(petId, chatHistory, pet.persona());
        
        if (aiResult.hasError()) {
            log.error("AI message generation failed: {}", aiResult.errorMessage());
            throw new NotificationException("AI 메시지 생성 실패: " + aiResult.errorMessage());
        }
        log.debug("AI message generated successfully: title='{}'", aiResult.title());
        
        // 5. 알림 생성
        NotificationId notificationId = notificationDomainService.createInactivityNotification(
            userId, petId, chatRoomId, aiResult.title(), aiResult.content());
        
        log.info("Successfully created inactivity notification: {} for user: {}", 
                notificationId.getValue(), userId.getValue());
        return Optional.of(notificationId);
    }
    
    
}