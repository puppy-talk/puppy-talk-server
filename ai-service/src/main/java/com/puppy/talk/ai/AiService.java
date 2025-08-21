package com.puppy.talk.ai;

import com.puppy.talk.ai.provider.AiProvider;
import com.puppy.talk.ai.provider.AiProviderFactory;
import com.puppy.talk.ai.provider.dto.AiRequest;
import com.puppy.talk.ai.provider.dto.AiResponse;
import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 응답 생성을 담당하는 서비스 클래스
 * 
 * 레이어드 아키텍처에서 비즈니스 로직 계층에 위치하여
 * 다양한 AI 제공업체를 통한 응답 생성 기능을 제공합니다.
 * 
 * 📋 주요 책임:
 * ✅ 펫 페르소나 기반 AI 응답 생성
 * ✅ 비활성 알림 메시지 생성
 * ✅ AI 제공업체 선택 및 관리
 * ✅ 대체 응답 제공 (Fallback)
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiService implements AiLookUpService {

    private final AiProviderFactory providerFactory;
    private final PromptBuilder promptBuilder;

    @Value("${ai.default-model:gpt-oss:20b}")
    private String defaultModel;

    @Value("${ai.max-tokens:150}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.8}")
    private Double temperature;

    /**
     * 펫의 페르소나와 채팅 히스토리를 기반으로 AI 응답을 생성합니다.
     *
     * @param pet 반려동물 정보
     * @param persona 반려동물의 성격 정보
     * @param userMessage 사용자가 보낸 메시지
     * @param chatHistory 최근 채팅 히스토리 (컨텍스트 제공용)
     * @return AI가 생성한 펫의 응답 메시지
     * @throws IllegalArgumentException 입력 값이 유효하지 않은 경우
     */
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

    /**
     * 비활성 상태 알림을 위한 AI 메시지를 생성합니다.
     *
     * @param pet 반려동물 정보
     * @param persona 반려동물의 성격 정보
     * @param recentMessages 최근 메시지들 (컨텍스트 제공용)
     * @return AI가 생성한 비활성 알림 메시지
     * @throws IllegalArgumentException 입력 값이 유효하지 않은 경우
     */
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

    /**
     * 범용 AI 응답 생성 (페르소나만 사용)
     *
     * @param messages 메시지 목록
     * @param persona 페르소나 정보
     * @return AI가 생성한 응답 메시지
     */
    @Override
    public String generateResponse(List<String> messages, Persona persona) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages cannot be null or empty");
        }
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        
        try {
            // 메시지 목록을 단일 컨텍스트로 결합
            String userMessage = String.join("\n", messages);
            
            // 프롬프트 생성 (Pet 없이)
            String prompt = promptBuilder.buildPrompt(null, persona, userMessage, null);
            
            // AI 요청 및 응답 생성
            AiProvider provider = providerFactory.getAvailableProvider();
            AiRequest request = AiRequest.of(prompt, defaultModel, maxTokens, temperature);
            AiResponse response = provider.generateResponse(request);
            
            return response.content();
            
        } catch (Exception e) {
            log.error("Failed to generate AI response with persona: {} - {}", persona.name(), e.getMessage(), e);
            return generateFallbackResponseForPersona(persona);
        }
    }
    
    // === AiLookUpService Interface Implementation ===
    
    @Override
    public String generateInactivityMessage(List<String> messages, Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        
        try {
            // Convert message strings to context string
            String messageContext = messages != null && !messages.isEmpty() 
                ? String.join("\n", messages) 
                : "No recent conversation";
                
            // Build inactivity-specific prompt
            String inactivityPrompt = String.format(
                "사용자와 오랫동안 대화하지 않았습니다. %s 페르소나의 성격에 맞게 먼저 대화를 시작해주세요. " +
                "이전 대화 내용: %s",
                persona.name(),
                messageContext
            );
            
            // Generate AI response
            AiProvider provider = providerFactory.getAvailableProvider();
            AiRequest request = AiRequest.of(inactivityPrompt, defaultModel, maxTokens, temperature);
            AiResponse response = provider.generateResponse(request);
            
            return response.content();
            
        } catch (Exception e) {
            log.error("Failed to generate inactivity message with persona: {} - {}", persona.name(), e.getMessage(), e);
            return generateFallbackResponseForPersona(persona);
        }
    }

    // === Private Helper Methods ===

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
            log.debug("Using AI provider: {} for pet: {}", provider.getProviderName(), pet.name());
            
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
        String personalityTraits = persona.personalityTraits() != null ? 
            persona.personalityTraits() : PromptBuilder.DEFAULT_PERSONALITY;
            
        String inactivityContext = String.format(
            "사용자가 %s와 오랫동안 대화하지 않았습니다. %s의 성격(%s)에 맞게 먼저 대화를 시작해주세요.",
            pet.name(),
            pet.name(),
            personalityTraits
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
    
    /**
     * 페르소나용 대체 응답을 생성합니다.
     */
    private String generateFallbackResponseForPersona(Persona persona) {
        return "안녕하세요! " + persona.name() + " 페르소나로 대화하고 있어요. 잠시 후 다시 시도해주세요! 😊";
    }
}