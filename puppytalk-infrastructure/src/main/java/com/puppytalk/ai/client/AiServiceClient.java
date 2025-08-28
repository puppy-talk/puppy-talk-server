package com.puppytalk.ai.client;

import com.puppytalk.ai.client.dto.ChatRequest;
import com.puppytalk.ai.client.dto.ChatResponse;
import com.puppytalk.ai.client.dto.InactivityNotificationRequest;
import com.puppytalk.ai.client.dto.InactivityNotificationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * PuppyTalk AI 서비스 클라이언트
 */
@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestClient restClient;

    public AiServiceClient(@Value("${ai-service.url:http://localhost:8001}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();

        log.info("AiServiceClient initialized with baseUrl: {}", baseUrl);
    }

    /**
     * 채팅 응답 생성
     */
    public ChatResponse generateChatResponse(ChatRequest request) {
        log.debug("Generating chat response for user: {}, pet: {}",
            request.userId(), request.petId());

        ChatResponse response = restClient.post()
            .uri("/api/v1/chat/generate")
            .body(request)
            .retrieve()
            .body(ChatResponse.class);

        log.debug("Chat response generated successfully");
        return response;
    }

    /**
     * 비활성 알림 메시지 생성
     */
    public InactivityNotificationResponse generateInactivityNotification(
        InactivityNotificationRequest request) {
        log.debug("Generating inactivity notification for user: {}, pet: {}",
            request.userId(), request.petId());

        try {
            InactivityNotificationResponse response = restClient.post()
                .uri("/api/v1/chat/inactivity-notification")
                .body(request)
                .retrieve()
                .body(InactivityNotificationResponse.class);

            log.debug("Inactivity notification generated successfully");
            return response;

        } catch (Exception e) {
            log.error("Failed to generate inactivity notification: {}", e.getMessage());
            throw new RuntimeException("AI 서비스 호출 실패", e);
        }
    }
}