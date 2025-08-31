package com.puppytalk.ai;

import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.Message;
import com.puppytalk.pet.Pet;

import java.util.List;

public interface AiMessageGenerationService {

    /**
     * 채팅 응답 생성
     * 
     * @param chatRoom 채팅방 정보
     * @param pet 반려동물 정보
     * @param userMessage 사용자 메시지
     * @param conversationHistory 대화 히스토리
     * @return 생성된 AI 응답
     */
    String generateChatResponse(ChatRoom chatRoom, Pet pet, String userMessage,
                               List<Message> conversationHistory);

    /**
     * 비활성 알림 생성
     * 
     * @param chatRoom 채팅방 정보
     * @param pet 반려동물 정보
     * @param hoursSinceLastActivity 마지막 활동 이후 시간
     * @param lastMessages 마지막 메시지들
     * @return 생성된 비활성 알림 메시지
     */
    String generateInactivityNotification(ChatRoom chatRoom, Pet pet,
                                        int hoursSinceLastActivity, List<Message> lastMessages);
}