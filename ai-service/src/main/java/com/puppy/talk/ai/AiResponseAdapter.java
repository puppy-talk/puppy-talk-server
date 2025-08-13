package com.puppy.talk.ai;

import com.puppy.talk.ai.provider.AiProvider;
import com.puppy.talk.ai.provider.AiProviderFactory;
import com.puppy.talk.ai.provider.AiRequest;
import com.puppy.talk.ai.provider.AiResponse;
import com.puppy.talk.infrastructure.ai.AiResponseGenerationException;
import com.puppy.talk.infrastructure.ai.AiResponsePort;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 응답 생성 포트의 구현체
 * Hexagonal Architecture에서 AI 응답 생성 기능을 제공하는 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiResponseAdapter implements AiResponsePort {

    private final AiProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;

    @Value("${ai.default-model:gpt-oss:20b}")
    private String defaultModel;

    @Value("${ai.max-tokens:150}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.8}")
    private Double temperature;

    @Override
    public String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory) {
        validateInputs(pet, persona, userMessage);

        try {
            log.debug("Generating AI response for pet: {} with persona: {}", pet.name(), persona.name());
            
            // 프롬프트 생성
            String prompt = promptBuilder.buildPrompt(pet, persona, userMessage, chatHistory);
            
            return generateAiResponse(prompt, pet);
            
        } catch (Exception e) {
            log.error("Failed to generate AI response for pet: {} - {}", pet.name(), e.getMessage(), e);
            return generateFallbackResponse(pet);
        }
    }

    @Override
    public String generateInactivityMessage(Pet pet, Persona persona, List<Message> recentMessages) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }

        try {
            log.debug("Generating inactivity message for pet: {} with persona: {}", pet.name(), persona.name());
            
            // 비활성 상황에 특화된 프롬프트 생성
            String inactivityPrompt = createInactivityPrompt(pet, persona, recentMessages);
            
            return generateAiResponse(inactivityPrompt, pet);
            
        } catch (Exception e) {
            log.error("Failed to generate inactivity message for pet: {} - {}", pet.name(), e.getMessage(), e);
            return generateInactivityFallbackResponse(pet);
        }
    }

    private void validateInputs(Pet pet, Persona persona, String userMessage) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }
    }

    private String generateAiResponse(String prompt, Pet pet) {
        try {
            // 사용 가능한 AI 제공업체 선택
            AiProvider provider = providerFactory.getAvailableProvider();
            log.debug("Using AI provider: {}", provider.getProviderName());
            
            // AI 요청 생성
            AiRequest request = AiRequest.of(prompt, defaultModel, maxTokens, temperature);
            
            // AI 응답 생성
            AiResponse response = provider.generateResponse(request);
            
            log.debug("Generated AI response from {}: {}", provider.getProviderName(), response.content());
            return response.content();
            
        } catch (Exception e) {
            throw new AiResponseGenerationException("Failed to generate AI response for pet: " + pet.name(), e);
        }
    }

    private String createInactivityPrompt(Pet pet, Persona persona, List<Message> recentMessages) {
        // 비활성 상황에 특화된 프롬프트 생성 로직
        String inactivityContext = String.format(
            "사용자가 %s와 오랫동안 대화하지 않았습니다. %s의 성격(%s)에 맞게 먼저 대화를 시작해주세요.",
            pet.name(),
            pet.name(),
            persona.personalityTraits()
        );
        
        return promptBuilder.buildInactivityPrompt(pet, persona, inactivityContext, recentMessages);
    }

    /**
     * AI 응답 생성에 실패했을 때 사용할 대체 응답을 생성합니다.
     */
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

    /**
     * 비활성 메시지 생성에 실패했을 때 사용할 대체 응답을 생성합니다.
     */
    private String generateInactivityFallbackResponse(Pet pet) {
        String[] inactivityFallbackResponses = {
            pet.name() + "이(가) 당신이 그리워요! 어떻게 지내고 있나요? 🥺",
            "안녕하세요! " + pet.name() + "이(가) 당신과 대화하고 싶어해요! 😊",
            pet.name() + "이(가) 꼬리를 흔들며 당신을 기다리고 있었어요! 🐾",
            "오랜만이에요! " + pet.name() + "이(가) 당신의 이야기를 듣고 싶어해요! ✨"
        };
        
        int randomIndex = (int) (Math.random() * inactivityFallbackResponses.length);
        return inactivityFallbackResponses[randomIndex];
    }
}