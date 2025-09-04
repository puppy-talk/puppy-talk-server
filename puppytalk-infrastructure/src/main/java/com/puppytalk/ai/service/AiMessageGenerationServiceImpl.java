package com.puppytalk.ai.service;

import com.puppytalk.ai.AiMessageGenerationService;
import com.puppytalk.ai.ChatContext;
import com.puppytalk.ai.client.AiServiceClient;
import com.puppytalk.ai.client.dto.ChatMessage;
import com.puppytalk.ai.client.dto.ChatRequest;
import com.puppytalk.ai.client.dto.ChatResponse;
import com.puppytalk.ai.client.dto.InactivityNotificationRequest;
import com.puppytalk.ai.client.dto.InactivityNotificationResponse;
import com.puppytalk.ai.client.dto.MessageRole;
import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.Message;
import com.puppytalk.pet.Pet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiMessageGenerationServiceImpl implements AiMessageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(AiMessageGenerationServiceImpl.class);

    private final AiServiceClient aiServiceClient;

    public AiMessageGenerationServiceImpl(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    public String generateChatResponse(ChatContext context) {
        log.info("Generating chat response for userId: {}, petId: {}", context.userId(), context.petId());

        ChatRequest request = new ChatRequest(
            context.userId().intValue(),
            context.petId().intValue(),
            context.userMessage(),
            context.petPersona(),
            convertToAiChatMessage(context.conversationHistory())
        );

        ChatResponse response = aiServiceClient.generateChatResponse(request);
        
        if (response.success() && response.content() != null) {
            return response.content();
        }
        
        throw new RuntimeException("AI 응답 생성 실패");
    }

    @Override
    public String generateInactivityNotification(ChatRoom chatRoom, Pet pet,
                                               int hoursSinceLastActivity, List<Message> lastMessages) {
        log.info("Generating inactivity notification for userId: {}, petId: {}", chatRoom.getUserId(), pet.getId());

        InactivityNotificationRequest request = new InactivityNotificationRequest(
            chatRoom.getUserId().value().intValue(),
            pet.getId().value().intValue(),
            pet.getPersona(),
            convertToAiChatMessage(lastMessages),
            hoursSinceLastActivity,
            getCurrentTimeOfDay()
        );

        InactivityNotificationResponse response = aiServiceClient.generateInactivityNotification(request);
        
        if (response.success() && response.notificationMessage() != null) {
            return response.notificationMessage();
        }
        
        throw new RuntimeException("비활성 알림 생성 실패");
    }


    private List<ChatMessage> convertToAiChatMessage(List<Message> messages) {
        if (messages == null) {
            throw new IllegalArgumentException("메시지 목록은 필수입니다");
        }
        
        return messages.stream()
            .map(message -> new ChatMessage(
                message.isUserMessage() ? MessageRole.USER : MessageRole.ASSISTANT,
                message.getContent(),
                message.getCreatedAt().toString()
            ))
            .toList();
    }
    
    private String getCurrentTimeOfDay() {
        int hour = java.time.LocalTime.now().getHour();
        
        if (hour >= 6 && hour < 12) return "morning";
        if (hour >= 12 && hour < 18) return "afternoon"; 
        if (hour >= 18 && hour < 22) return "evening";
        return "night";
    }
}