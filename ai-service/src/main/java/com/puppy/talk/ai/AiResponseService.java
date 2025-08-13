package com.puppy.talk.ai;

import com.puppy.talk.ai.provider.AiProvider;
import com.puppy.talk.ai.provider.AiProviderFactory;
import com.puppy.talk.ai.provider.AiRequest;
import com.puppy.talk.ai.provider.AiResponse;
import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiResponseService {

    private final AiProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;

    @Value("${ai.default-model:gpt-oss:20b}")
    private String defaultModel;

    @Value("${ai.max-tokens:150}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.8}")
    private Double temperature;

    /**
     * 펫의 페르소나를 기반으로 사용자 메시지에 대한 AI 응답을 생성합니다.
     */
    public String generatePetResponse(Pet pet, Persona persona, String userMessage, List<Message> chatHistory) {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }

        try {
            log.debug("Generating AI response for pet: {} with persona: {}", pet.name(), persona.name());
            
            // 프롬프트 생성
            String prompt = promptBuilder.buildPrompt(pet, persona, userMessage, chatHistory);
            
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
            log.error("Failed to generate AI response for pet: {} - {}", pet.name(), e.getMessage(), e);
            return generateFallbackResponse(pet);
        }
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
}
