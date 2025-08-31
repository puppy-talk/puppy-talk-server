package com.puppytalk.notification;

import com.puppytalk.ai.AiMessageGenerationService;
import com.puppytalk.chat.ChatDomainService;
import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.Message;
import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserId;
import java.util.List;
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
    private static final int CHAT_HISTORY_LIMIT = 20;
    
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
    public void createInactivityNotification(Long userId, Long petId) {
        if (userId == null || petId == null) {
            log.warn("Invalid parameters: userId={}, petId={}", userId, petId);
            return;
        }

        // 1. 반려동물 정보 조회
        Pet pet = petRepository.findById(PetId.from(petId))
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));

        // 2. 채팅방 조회
        ChatRoom chatRoom = chatDomainService.findChatRoomByUserIdAndPetId(
            UserId.from(userId),
            PetId.from(petId)
        ).orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

        // 3. 최근 채팅 히스토리 조회
        ChatRoomId chatRoomId = chatRoom.id();
        List<Message> chatHistory = chatDomainService.findRecentChatHistory(chatRoomId, CHAT_HISTORY_LIMIT);

        // 4. AI 메시지 생성
        String aiMessage = aiMessageGenerationService.generateInactivityNotification(
            chatRoom,
            pet,
            2,
            chatHistory
        );

        // 5. 알림 생성
        notificationDomainService.createInactivityNotification(
            UserId.from(userId), PetId.from(petId), chatRoomId, "반려동물 메시지", aiMessage
        );
    }
}