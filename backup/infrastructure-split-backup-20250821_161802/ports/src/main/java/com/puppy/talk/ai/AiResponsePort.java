package com.puppy.talk.ai;

import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;

import java.util.List;

/**
 * AI 응답 생성을 위한 포트 인터페이스
 * Hexagonal Architecture에서 Service 레이어가 AI 기능에 접근하기 위한 추상화
 */
public interface AiResponsePort {
    
    /**
     * 펫의 페르소나와 채팅 히스토리를 기반으로 AI 응답을 생성합니다.
     *
     * @param pet 반려동물 정보
     * @param persona 반려동물의 성격 정보
     * @param userMessage 사용자가 보낸 메시지
     * @param chatHistory 최근 채팅 히스토리 (컨텍스트 제공용)
     * @return AI가 생성한 펫의 응답 메시지
     * @throws AiResponseGenerationException AI 응답 생성 실패 시
     */
    String generatePetResponse(
        Pet pet, 
        Persona persona, 
        String userMessage, 
        List<Message> chatHistory
    );
    
    /**
     * 비활성 상태 알림을 위한 AI 메시지를 생성합니다.
     *
     * @param pet 반려동물 정보
     * @param persona 반려동물의 성격 정보
     * @param recentMessages 최근 메시지들 (컨텍스트 제공용)
     * @return AI가 생성한 비활성 알림 메시지
     * @throws AiResponseGenerationException AI 응답 생성 실패 시
     */
    String generateInactivityMessage(
        Pet pet,
        Persona persona,
        List<Message> recentMessages
    );
}