package com.puppy.talk.notification;

import com.puppy.talk.ai.AiService;
import com.puppy.talk.activity.InactivityNotification;
import com.puppy.talk.activity.InactivityNotificationIdentity;
import com.puppy.talk.activity.NotificationStatus;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.SenderType;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.pet.Persona;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.pet.service.PersonaLookUpService;
import com.puppy.talk.notification.dto.InactivityNotificationStatistics;
import com.puppy.talk.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private final InactivityNotificationDomainService inactivityNotificationDomainService;
    private final PersonaLookUpService personaLookUpService;
    private final AiService aiService;
    private final PushNotificationService pushNotificationService;

    private static final int AI_CONTEXT_MESSAGE_LIMIT = 5;

    /**
     * 알림 대상이 된 비활성 알림들을 처리합니다.
     * 스케줄러에서 주기적으로 호출됩니다.
     */
    public void processEligibleNotifications() {
        log.debug("Processing eligible inactivity notifications...");
        
        List<InactivityNotification> eligibleNotifications = 
            inactivityNotificationDomainService.findEligibleNotifications();
            
        if (eligibleNotifications.isEmpty()) {
            log.debug("No eligible notifications found");
            return;
        }
        
        log.info("Found {} eligible notifications to process", eligibleNotifications.size());
        
        int successCount = 0;
        for (InactivityNotification notification : eligibleNotifications) {
            if (processSingleNotificationSafely(notification)) {
                successCount++;
            }
        }
        
        log.info("Successfully processed {}/{} inactivity notifications", 
            successCount, eligibleNotifications.size());
    }

    /**
     * 단일 비활성 알림을 안전하게 처리합니다.
     */
    private boolean processSingleNotificationSafely(InactivityNotification notification) {
        try {
            return processSingleNotification(notification);
        } catch (Exception e) {
            log.error("Failed to process inactivity notification for chatRoomId={}: {}", 
                notification.chatRoomId().id(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 단일 비활성 알림을 처리합니다.
     */
    private boolean processSingleNotification(InactivityNotification notification) {
        log.debug("Processing notification for chatRoomId={}", notification.chatRoomId().id());
        
        ChatRoom chatRoom = findChatRoomOrSkip(notification.chatRoomId());
        if (chatRoom == null) return false;
        
        Pet pet = findPetOrSkip(chatRoom.petId());
        if (pet == null) return false;
        
        String aiMessage = generateInactivityMessageWithFallback(pet, chatRoom);
        
        // 알림 업데이트 및 메시지 저장
        updateNotificationWithMessage(notification, aiMessage);
        Message savedPetMessage = saveInactivityMessage(chatRoom.identity(), aiMessage);
        
        // 실시간 통신 및 푸시 알림
        sendRealtimeCommunications(chatRoom, pet, savedPetMessage, aiMessage);
        
        // 알림 상태 완료로 변경
        markNotificationAsSent(notification, aiMessage);
        
        log.info("Successfully sent inactivity notification for chatRoomId={}, petName={}", 
            chatRoom.identity().id(), pet.name());
        return true;
    }

    /**
     * 비활성 상황에 맞는 AI 메시지를 생성합니다 (Fallback 포함).
     */
    private String generateInactivityMessageWithFallback(Pet pet, ChatRoom chatRoom) {
        try {
            return generateInactivityMessage(pet, chatRoom);
        } catch (Exception e) {
            log.warn("AI inactivity message generation failed for pet={}, using fallback: {}", 
                pet.name(), e.getMessage());
            return createDefaultInactivityMessage(pet);
        }
    }
    
    /**
     * 비활성 상황에 맞는 AI 메시지를 생성합니다.
     */
    private String generateInactivityMessage(Pet pet, ChatRoom chatRoom) {
        Persona persona = personaLookUpService.findPersona(pet.personaId());
        
        List<Message> chatHistory = inactivityNotificationDomainService
            .findRecentMessages(chatRoom.identity(), AI_CONTEXT_MESSAGE_LIMIT)
            .stream()
            .toList();
        
        return aiService.generateInactivityMessage(pet, persona, chatHistory);
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
     * 비활성 알림에 대한 푸시 알림을 전송합니다.
     */
    private void sendPushNotification(Pet pet, String message) {
        try {
            String pushTitle = pet.name() + "이(가) 보고 싶어해요! 🐾";
            String pushMessage = shortenMessageForPush(message);
            
            // 푸시 알림 데이터 생성 (JSON 형태)
            String pushData = String.format(
                "{\"petId\":%d,\"petName\":\"%s\",\"chatAction\":\"open\"}",
                pet.identity().id(),
                pet.name()
            );
            
            // 푸시 알림 전송
            pushNotificationService.sendNotification(
                pet.userId(),
                NotificationType.INACTIVITY_MESSAGE,
                pushTitle,
                pushMessage,
                pushData
            );
            
            log.debug("Sent push notification for pet={}, user={}", pet.name(), pet.userId().id());
            
        } catch (Exception e) {
            log.warn("Failed to send push notification for pet={}: {}", pet.name(), e.getMessage());
            // 푸시 알림 실패는 전체 프로세스를 중단시키지 않음
        }
    }
    
    /**
     * WebSocket을 통해 실시간 메시지를 브로드캐스트합니다.
     */
    private void sendWebSocketMessage(ChatRoom chatRoom, Pet pet, Message savedMessage, String content) {
        try {
            ChatMessage webSocketMessage = ChatMessage.newMessage(
                savedMessage.identity(),
                chatRoom.identity(),
                pet.userId(),
                SenderType.PET,
                content,
                false
            );
            
            // WebSocket 브로드캐스트는 별도 서비스에서 처리
            log.debug("WebSocket message prepared for pet={}, chatRoom={}", pet.name(), chatRoom.identity().id());
            
            log.debug("Sent WebSocket message for pet={}, chatRoom={}", pet.name(), chatRoom.identity().id());
            
        } catch (Exception e) {
            log.warn("Failed to send WebSocket message for pet={}: {}", pet.name(), e.getMessage());
            // WebSocket 실패는 전체 프로세스를 중단시키지 않음
        }
    }
    
    /**
     * 푸시 알림용으로 메시지를 단축합니다.
     */
    private String shortenMessageForPush(String message) {
        if (message == null) {
            return "";
        }
        
        // 이모지와 특수 문자 제거 (Java에서 지원하는 패턴 사용)
        String cleaned = message
            .replaceAll("[\\p{So}\\p{Sc}]", "")  // 기호 문자들 제거
            .replaceAll("[🐾💕✨🌟🤔]", "")      // 흔한 이모지들 직접 제거
            .trim();
        
        // 100자 이내로 제한
        if (cleaned.length() > 100) {
            return cleaned.substring(0, 97) + "...";
        }
        
        return cleaned;
    }

    /**
     * 특정 채팅방의 비활성 알림을 비활성화합니다.
     */
    public void disableNotification(ChatRoom chatRoom) {
        inactivityNotificationDomainService.disableNotificationForChatRoom(chatRoom);
    }

    /**
     * 비활성 알림 상태 통계를 조회합니다.
     */
    public InactivityNotificationStatistics getStatistics() {
        return inactivityNotificationDomainService.getNotificationStatistics();
    }

    // === Helper Methods for Improved Readability and Error Handling ===
    
    private ChatRoom findChatRoomOrSkip(ChatRoomIdentity chatRoomId) {
        Optional<ChatRoom> chatRoomOpt = inactivityNotificationDomainService.findChatRoom(chatRoomId);
        if (chatRoomOpt.isEmpty()) {
            log.warn("ChatRoom not found for notification: {}", chatRoomId.id());
            return null;
        }
        return chatRoomOpt.get();
    }
    
    private Pet findPetOrSkip(PetIdentity petId) {
        Optional<Pet> petOpt = inactivityNotificationDomainService.findPet(petId);
        if (petOpt.isEmpty()) {
            log.warn("Pet not found for petId: {}", petId.id());
            return null;
        }
        return petOpt.get();
    }
    
    private void updateNotificationWithMessage(InactivityNotification notification, String aiMessage) {
        InactivityNotification updatedNotification = notification.withAiGeneratedMessage(aiMessage);
        inactivityNotificationDomainService.updateNotification(updatedNotification);
    }
    
    private Message saveInactivityMessage(ChatRoomIdentity chatRoomId, String content) {
        Message petMessage = Message.of(
            null, // identity는 저장 시 생성됨
            chatRoomId,
            SenderType.PET,
            content,
            false, // 펫 메시지는 처음에 읽지 않음 상태
            LocalDateTime.now()
        );
        return inactivityNotificationDomainService.saveMessage(petMessage);
    }
    
    private void sendRealtimeCommunications(ChatRoom chatRoom, Pet pet, Message savedMessage, String content) {
        sendWebSocketMessage(chatRoom, pet, savedMessage, content);
        sendPushNotification(pet, content);
    }
    
    private void markNotificationAsSent(InactivityNotification notification, String aiMessage) {
        InactivityNotification updatedNotification = notification.withAiGeneratedMessage(aiMessage);
        InactivityNotification sentNotification = updatedNotification.markAsSent();
        inactivityNotificationDomainService.markNotificationAsSent(notification);
    }
    
    // === NotificationLookUpService 인터페이스 구현 ===
    
    public void sendInactivityNotification(UserIdentity userId, LocalDateTime lastActivityTime) {
        // TODO: 구현 필요 - 현재는 processEligibleNotifications()로 처리
        log.debug("sendInactivityNotification called for user: {}", userId.id());
    }
    
    public List<InactivityNotification> getInactivityNotifications(UserIdentity userId) {
        // TODO: 구현 필요 - 사용자별 비활성 알림 조회
        log.debug("getInactivityNotifications called for user: {}", userId.id());
        return List.of();
    }
    
    public void markInactivityNotificationAsRead(InactivityNotificationIdentity notificationId) {
        // TODO: 구현 필요 - 비활성 알림 읽음 처리
        log.debug("markInactivityNotificationAsRead called for notification: {}", notificationId.id());
    }
}