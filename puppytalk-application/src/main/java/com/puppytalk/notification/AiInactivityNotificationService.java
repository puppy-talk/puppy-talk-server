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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * AI 기반 비활성 사용자 알림 서비스
 * 
 * Backend 관점: 비즈니스 로직 중심의 신뢰할 수 있는 AI 알림 생성
 */
public class AiInactivityNotificationService {
    
    private final NotificationDomainService notificationDomainService;
    private final ChatDomainService chatDomainService;
    private final PetRepository petRepository;
    private final AiMessageGenerationService aiMessageGenerationService;
    
    public AiInactivityNotificationService(
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
     * 비활성 사용자에게 AI 기반 반려동물 메시지 알림 생성
     * 
     * @param userId 비활성 사용자 ID
     * @param petId 반려동물 ID
     * @return 생성된 알림 ID (실패 시 Optional.empty())
     */
    @Transactional
    public Optional<NotificationId> createInactivityNotification(UserId userId, PetId petId) {
        try {
            // 1. 반려동물 정보 조회
            Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
            
            // 2. 채팅방 조회
            Optional<ChatRoom> chatRoom = chatDomainService.findChatRoomByUserAndPet(userId, petId);
            if (chatRoom.isEmpty()) {
                // 채팅방이 없으면 알림 생성 불가
                return Optional.empty();
            }
            
            ChatRoomId chatRoomId = chatRoom.get().getId();
            
            // 3. 최근 채팅 히스토리 조회 (최대 10개)
            List<Message> chatHistory = chatDomainService.findRecentChatHistory(chatRoomId, 10);
            
            // 4. AI 메시지 생성
            AiMessageGenerationService.AiMessageResult aiResult = 
                aiMessageGenerationService.generateInactivityMessage(petId, chatHistory, pet.getPersona());
            
            if (aiResult.hasError()) {
                throw new NotificationException.AiGenerationFailed(
                    "AI 메시지 생성 실패: " + aiResult.errorMessage());
            }
            
            // 5. 알림 생성
            NotificationId notificationId = notificationDomainService.createInactivityNotification(
                userId, petId, chatRoomId, aiResult.title(), aiResult.content());
            
            return Optional.of(notificationId);
            
        } catch (Exception e) {
            // 로깅 및 예외 처리
            throw new NotificationException.CreationFailed(
                "비활성 사용자 알림 생성 중 오류 발생", userId, e);
        }
    }
    
    /**
     * 다중 비활성 사용자에 대한 일괄 알림 생성
     * 
     * @param inactiveUsers 비활성 사용자와 반려동물 ID 목록
     * @return 생성 성공한 알림 개수
     */
    @Transactional
    public InactivityNotificationBatchResult createBatchInactivityNotifications(
        List<UserPetPair> inactiveUsers) {
        
        int successCount = 0;
        int failureCount = 0;
        
        for (UserPetPair userPet : inactiveUsers) {
            try {
                Optional<NotificationId> result = createInactivityNotification(
                    userPet.userId(), userPet.petId());
                
                if (result.isPresent()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
                // 개별 실패는 로깅만 하고 계속 진행
            }
        }
        
        return new InactivityNotificationBatchResult(successCount, failureCount);
    }
    
    /**
     * 사용자-반려동물 쌍
     */
    public record UserPetPair(UserId userId, PetId petId) {
        
        public static UserPetPair of(Long userId, Long petId) {
            return new UserPetPair(UserId.of(userId), PetId.of(petId));
        }
    }
    
    /**
     * 일괄 알림 생성 결과
     */
    public record InactivityNotificationBatchResult(
        int successCount,
        int failureCount
    ) {
        
        public int totalCount() {
            return successCount + failureCount;
        }
        
        public double successRate() {
            return totalCount() == 0 ? 0.0 : (double) successCount / totalCount();
        }
        
        public boolean hasFailures() {
            return failureCount > 0;
        }
    }
}