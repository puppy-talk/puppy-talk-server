package com.puppy.talk.ai;

import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.SenderType;
import com.puppy.talk.model.pet.Pet;
import com.puppy.talk.model.pet.Persona;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PromptBuilder {

    private static final String DEFAULT_BREED = "귀여운 반려동물";
    private static final String DEFAULT_PERSONALITY = "친근하고 활발한";
    private static final String DEFAULT_DESCRIPTION = "사랑스러운 반려동물";

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        당신은 반려동물 '%s'입니다. 다음과 같은 특성을 가지고 있습니다:
        
        이름: %s
        품종: %s
        나이: %d살
        성격: %s
        
        페르소나 설명: %s
        
        %s
        
        다음 규칙을 따라 응답해주세요:
        1. 항상 반려동물의 관점에서 대답하세요
        2. 성격과 페르소나에 맞는 톤과 스타일로 응답하세요
        3. 간결하고 자연스러운 대화를 유지하세요 (최대 2-3문장)
        4. 이모지를 적절히 사용하여 표현력을 높이세요
        5. 한국어로 응답하세요
        6. 반려동물답게 귀엽고 애정 넘치는 표현을 사용하세요
        """;

    /**
     * AI 모델에 전달할 프롬프트를 생성합니다.
     */
    public String buildPrompt(Pet pet, Persona persona, String userMessage, List<Message> chatHistory) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 시스템 프롬프트 추가
        String systemPrompt = String.format(
            SYSTEM_PROMPT_TEMPLATE,
            pet.name(),
            pet.name(),
            pet.breed() != null ? pet.breed() : DEFAULT_BREED,
            pet.age(),
            persona.personalityTraits() != null ? persona.personalityTraits() : DEFAULT_PERSONALITY,
            persona.description() != null ? persona.description() : DEFAULT_DESCRIPTION,
            persona.aiPromptTemplate() != null ? persona.aiPromptTemplate() : ""
        );
        
        promptBuilder.append(systemPrompt).append("\n\n");
        
        // 대화 히스토리 추가 (최근 5개만)
        if (chatHistory != null && !chatHistory.isEmpty()) {
            promptBuilder.append("최근 대화 기록:\n");
            
            List<Message> recentMessages = chatHistory.stream()
                .limit(5)
                .collect(Collectors.toList());
            
            for (Message message : recentMessages) {
                String sender = message.senderType() == SenderType.USER ? "사용자" : pet.name();
                promptBuilder.append(String.format("%s: %s\n", sender, message.content()));
            }
            promptBuilder.append("\n");
        }
        
        // 현재 사용자 메시지 추가
        promptBuilder.append("사용자: ").append(userMessage).append("\n");
        promptBuilder.append(pet.name()).append(": ");
        
        String finalPrompt = promptBuilder.toString();
        log.debug("Generated prompt: {}", finalPrompt);
        
        return finalPrompt;
    }
}
