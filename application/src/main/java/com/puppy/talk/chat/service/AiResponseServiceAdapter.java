package com.puppy.talk.chat.service;

import com.puppy.talk.ai.AiService;
import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 응답 서비스 어댑터
 * 
 * 도메인 레이어의 AiResponseService 인터페이스를 구현하여
 * 실제 AI 서비스와의 의존성을 관리합니다.
 * 
 * Application Layer에 위치하여 외부 서비스와 도메인 서비스를 연결하는 역할을 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiResponseServiceAdapter implements AiResponseService {

    private final AiService aiService;

    @Override
    public String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory) {
        log.debug("Delegating AI response generation to AiService for pet: {}", pet.name());
        
        try {
            return aiService.generatePetResponse(pet, persona, userMessage, chatHistory);
        } catch (Exception e) {
            log.error("Failed to generate AI response for pet: {} - {}", pet.name(), e.getMessage(), e);
            // 실패 시 기본 응답 반환
            return generateFallbackResponse(pet);
        }
    }

    private String generateFallbackResponse(Pet pet) {
        String[] fallbackResponses = {
            pet.name() + "이(가) 잠시 생각 중이에요... 🤔",
            "지금은 말을 할 수 없지만, " + pet.name() + "이(가) 당신을 생각하고 있어요! ❤️",
            pet.name() + "이(가) 꼬리를 흔들며 당신을 바라보고 있어요! 🐕",
            "음... " + pet.name() + "이(가) 무언가 말하고 싶어하는 것 같아요! 🐾"
        };
        
        int randomIndex = (int) (Math.random() * fallbackResponses.length);
        return fallbackResponses[randomIndex];
    }
}