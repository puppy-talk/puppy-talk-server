package com.puppytalk.ai;

import com.puppytalk.chat.Message;
import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

import java.util.List;

/**
 * AI 메시지 생성 서비스 인터페이스
 */
public interface AiMessageGenerationService {

    /**
     * 채팅 응답 생성
     * 
     * @param userId 사용자 ID
     * @param petId 반려동물 ID
     * @param pet 반려동물 정보
     * @param userMessage 사용자 메시지
     * @param conversationHistory 대화 히스토리
     * @return 생성된 AI 응답
     */
    String generateChatResponse(UserId userId, PetId petId, Pet pet, 
                               String userMessage, List<Message> conversationHistory);

    /**
     * 비활성 알림 생성
     * 
     * @param userId 사용자 ID
     * @param petId 반려동물 ID
     * @param pet 반려동물 정보
     * @param hoursSinceLastActivity 마지막 활동 이후 시간
     * @param lastMessages 마지막 메시지들
     * @return 생성된 비활성 알림 메시지
     */
    String generateInactivityNotification(UserId userId, PetId petId, Pet pet, 
                                        int hoursSinceLastActivity, List<Message> lastMessages);
}