package com.puppy.talk.service;

import com.puppy.talk.ai.AiResponseService;
import com.puppy.talk.infrastructure.activity.InactivityNotificationRepository;
import com.puppy.talk.infrastructure.chat.ChatRoomRepository;
import com.puppy.talk.infrastructure.chat.MessageRepository;
import com.puppy.talk.infrastructure.pet.PetRepository;
import com.puppy.talk.model.activity.InactivityNotification;
import com.puppy.talk.model.activity.NotificationStatus;
import com.puppy.talk.model.chat.ChatRoom;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 비활성 알림 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InactivityNotificationService {

    private final InactivityNotificationRepository inactivityNotificationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PetRepository petRepository;
    private final MessageRepository messageRepository;
    private final PersonaLookUpService personaLookUpService;
    private final AiResponseService aiResponseService;

    private static final int AI_CONTEXT_MESSAGE_LIMIT = 5;

    /**
     * 알림 대상이 된 비활성 알림들을 처리합니다.
     * 스케줄러에서 주기적으로 호출됩니다.
     */
    @Transactional
    public void processEligibleNotifications() {
        log.debug("Processing eligible inactivity notifications...");
        
        List<InactivityNotification> eligibleNotifications = 
            inactivityNotificationRepository.findEligibleNotifications();
            
        if (eligibleNotifications.isEmpty()) {
            log.debug("No eligible notifications found");
            return;
        }
        
        log.info("Found {} eligible notifications to process", eligibleNotifications.size());
        
        for (InactivityNotification notification : eligibleNotifications) {
            processSingleNotification(notification);
        }
        
        log.info("Processed {} inactivity notifications", eligibleNotifications.size());
    }

    /**
     * 단일 비활성 알림을 처리합니다.
     */
    private void processSingleNotification(InactivityNotification notification) {
        log.debug("Processing notification for chatRoomId={}", notification.chatRoomId().id());
        
        // 채팅방 조회
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByIdentity(notification.chatRoomId());
        if (chatRoomOpt.isEmpty()) {
            log.warn("ChatRoom not found for notification: {}", notification.chatRoomId().id());
            return;
        }
        
        ChatRoom chatRoom = chatRoomOpt.get();
        
        // 펫 조회
        Optional<Pet> petOpt = petRepository.findByIdentity(chatRoom.petId());
        if (petOpt.isEmpty()) {
            log.warn("Pet not found for chatRoom: {}", chatRoom.petId().id());
            return;
        }
        
        Pet pet = petOpt.get();
        
        // AI 메시지 생성
        String aiMessage = generateInactivityMessage(pet, chatRoom);
        
        // AI 메시지를 비활성 알림에 저장
        InactivityNotification updatedNotification = notification.withAiGeneratedMessage(aiMessage);
        inactivityNotificationRepository.save(updatedNotification);
        
        // 펫 메시지로 저장
        Message petMessage = Message.of(
            null, // identity는 저장 시 생성됨
            chatRoom.identity(),
            SenderType.PET,
            aiMessage,
            false, // 펫 메시지는 처음에 읽지 않음 상태
            LocalDateTime.now()
        );
        
        messageRepository.save(petMessage);
        
        // 알림을 SENT 상태로 변경
        InactivityNotification sentNotification = updatedNotification.markAsSent();
        inactivityNotificationRepository.save(sentNotification);
        
        log.info("Successfully sent inactivity notification for chatRoomId={}, petName={}", 
            chatRoom.identity().id(), pet.name());
    }

    /**
     * 비활성 상황에 맞는 AI 메시지를 생성합니다.
     */
    private String generateInactivityMessage(Pet pet, ChatRoom chatRoom) {
        // 페르소나 조회
        Persona persona = personaLookUpService.findPersona(pet.personaId());
        
        // 채팅 히스토리 조회 (최근 메시지들)
        List<Message> chatHistory = messageRepository
            .findByChatRoomIdOrderByCreatedAtDesc(chatRoom.identity())
            .stream()
            .limit(AI_CONTEXT_MESSAGE_LIMIT)
            .toList();
        
        // 비활성 상황에 특화된 사용자 메시지 생성
        String inactivityPrompt = createInactivityPrompt(pet);
        
        // AI 응답 생성
        return aiResponseService.generatePetResponse(pet, persona, inactivityPrompt, chatHistory);
    }

    /**
     * 비활성 상황에 특화된 프롬프트를 생성합니다.
     */
    private String createInactivityPrompt(Pet pet) {
        return String.format(
            "%s이(가) 오랫동안 대화하지 않아서 궁금해하며 먼저 말을 걸어보세요. " +
            "친근하고 자연스럽게 안부를 묻거나 재미있는 이야기를 시작해보세요.", 
            pet.name()
        );
    }

    /**
     * AI 실패 시 사용할 기본 비활성 메시지를 생성합니다.
     */
    private String createDefaultInactivityMessage(Pet pet) {
        String[] defaultMessages = {
            "안녕! 오랜만이야~ 뭐하고 있었어? 🐾",
            "심심해서 왔어! 같이 놀자! ✨",
            "보고 싶었는데, 어떻게 지냈어? 💕",
            "오늘 하루 어땠어? 나한테 얘기해줘! 🌟",
            "궁금한 게 생겼어! 지금 시간 있어? 🤔"
        };
        
        int index = (int) (Math.random() * defaultMessages.length);
        return defaultMessages[index];
    }

    /**
     * 특정 채팅방의 비활성 알림을 비활성화합니다.
     */
    @Transactional
    public void disableNotification(ChatRoom chatRoom) {
        Optional<InactivityNotification> notificationOpt = 
            inactivityNotificationRepository.findByChatRoomId(chatRoom.identity());
            
        if (notificationOpt.isPresent()) {
            InactivityNotification disabledNotification = notificationOpt.get().disable();
            inactivityNotificationRepository.save(disabledNotification);
            
            log.debug("Disabled inactivity notification for chatRoomId={}", chatRoom.identity().id());
        }
    }

    /**
     * 비활성 알림 상태 통계를 조회합니다.
     */
    @Transactional(readOnly = true)
    public NotificationStatistics getStatistics() {
        long totalCount = inactivityNotificationRepository.count();
        long pendingCount = inactivityNotificationRepository.countByStatus(NotificationStatus.PENDING);
        long sentCount = inactivityNotificationRepository.countByStatus(NotificationStatus.SENT);
        long disabledCount = inactivityNotificationRepository.countByStatus(NotificationStatus.DISABLED);
        
        return new NotificationStatistics(totalCount, pendingCount, sentCount, disabledCount);
    }

    /**
     * 비활성 알림 상태 통계 DTO
     */
    public record NotificationStatistics(
        long totalCount,
        long pendingCount, 
        long sentCount,
        long disabledCount
    ) {}
}