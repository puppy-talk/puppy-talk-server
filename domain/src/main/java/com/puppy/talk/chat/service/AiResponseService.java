package com.puppy.talk.chat.service;

import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;

import java.util.List;

/**
 * AI 응답 생성을 위한 도메인 서비스 인터페이스
 * 
 * 도메인 레이어에서 AI 서비스를 추상화하여 의존성 역전을 구현합니다.
 * 실제 구현체는 인프라스트럭처 레이어에서 제공됩니다.
 */
public interface AiResponseService {

    /**
     * 펫의 페르소나와 채팅 히스토리를 기반으로 AI 응답을 생성합니다.
     *
     * @param pet 반려동물 정보
     * @param persona 반려동물의 성격 정보
     * @param userMessage 사용자가 보낸 메시지
     * @param chatHistory 최근 채팅 히스토리 (컨텍스트 제공용)
     * @return AI가 생성한 펫의 응답 메시지
     */
    String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory);
}