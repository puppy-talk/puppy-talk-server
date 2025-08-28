package com.puppytalk.ai.service;

import com.puppytalk.ai.AiMessageGenerationService;
import com.puppytalk.ai.client.AiServiceClient;
import com.puppytalk.ai.client.dto.ChatMessage;
import com.puppytalk.ai.client.dto.ChatRequest;
import com.puppytalk.ai.client.dto.ChatResponse;
import com.puppytalk.ai.client.dto.InactivityNotificationRequest;
import com.puppytalk.ai.client.dto.InactivityNotificationResponse;
import com.puppytalk.ai.client.dto.MessageRole;
import com.puppytalk.ai.client.dto.PersonaType;
import com.puppytalk.ai.client.dto.PetPersona;
import com.puppytalk.chat.Message;
import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiMessageGenerationServiceImpl implements AiMessageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AiMessageGenerationServiceImpl.class);

    private final AiServiceClient aiServiceClient;

    public AiMessageGenerationServiceImpl(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    public String generateChatResponse(UserId userId, PetId petId, Pet pet, 
                                     String userMessage, List<Message> conversationHistory) {
        log.info("Generating chat response for user: {}, pet: {}", userId, petId);

        ChatRequest request = new ChatRequest(
            userId.getValue().intValue(),
            petId.getValue().intValue(),
            0, // ChatRoomId
            userMessage,
            buildPetPersona(pet),
            convertMessages(conversationHistory),
            Map.of(),
            150,
            0.8
        );

        ChatResponse response = aiServiceClient.generateChatResponse(request);
        
        if (response.success() && response.content() != null) {
            return response.content();
        }
        
        throw new RuntimeException("AI 응답 생성 실패");
    }

    @Override
    public String generateInactivityNotification(UserId userId, PetId petId, Pet pet, 
                                               int hoursSinceLastActivity, List<Message> lastMessages) {
        log.info("Generating inactivity notification for user: {}, pet: {}", userId, petId);

        InactivityNotificationRequest request = new InactivityNotificationRequest(
            userId.getValue().intValue(),
            petId.getValue().intValue(),
            0, // ChatRoomId
            buildPetPersona(pet),
            convertMessages(lastMessages),
            hoursSinceLastActivity,
            getTimeOfDay()
        );

        InactivityNotificationResponse response = aiServiceClient.generateInactivityNotification(request);
        
        if (response.success() && response.notificationMessage() != null) {
            return response.notificationMessage();
        }
        
        throw new RuntimeException("비활성 알림 생성 실패");
    }

    private PetPersona buildPetPersona(Pet pet) {
        return new PetPersona(
            getPersonaType(pet.persona()),
            pet.name(),
            null,
            null,
            List.of(),
            null
        );
    }

    private PersonaType getPersonaType(String persona) {
        if (persona == null) return PersonaType.FRIENDLY;
        
        try {
            return PersonaType.valueOf(persona.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PersonaType.FRIENDLY;
        }
    }

    private List<ChatMessage> convertMessages(List<Message> messages) {
        if (messages == null) return List.of();
        
        return messages.stream()
            .limit(20)
            .map(message -> new ChatMessage(
                message.type().isUserMessage() ? MessageRole.USER : MessageRole.ASSISTANT,
                message.content(),
                message.createdAt().toString()
            ))
            .toList();
    }

    private String getTimeOfDay() {
        int hour = java.time.LocalTime.now().getHour();
        
        if (hour >= 6 && hour < 12) return "morning";
        if (hour >= 12 && hour < 18) return "afternoon"; 
        if (hour >= 18 && hour < 22) return "evening";
        return "night";
    }
}