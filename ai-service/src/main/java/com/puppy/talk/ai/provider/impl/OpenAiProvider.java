package com.puppy.talk.ai.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppy.talk.ai.AiResponseException;
import com.puppy.talk.ai.provider.AiProvider;
import com.puppy.talk.ai.provider.AiRequest;
import com.puppy.talk.ai.provider.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * OpenAI ChatGPT 제공업체 구현
 * 
 * OpenAI API를 통해 ChatGPT 모델과 통신합니다.
 */
@Slf4j
@Component
public class OpenAiProvider implements AiProvider {

    private static final String PROVIDER_NAME = "openai";
    private static final String[] SUPPORTED_MODELS = {
        "gpt-4", "gpt-4-turbo", "gpt-4o", "gpt-4o-mini",
        "gpt-3.5-turbo", "gpt-3.5-turbo-16k"
    };

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.openai.api-key:}")
    private String apiKey;

    @Value("${ai.providers.openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${ai.providers.openai.timeout:30}")
    private Integer timeoutSeconds;

    @Value("${ai.providers.openai.enabled:false}")
    private Boolean enabled;

    public OpenAiProvider() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String[] getSupportedModels() {
        return SUPPORTED_MODELS.clone();
    }

    @Override
    public AiResponse generateResponse(AiRequest request) throws AiResponseException {
        if (!isEnabled()) {
            throw new AiResponseException("OpenAI provider is disabled");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AiResponseException("OpenAI API key is not configured");
        }

        try {
            log.debug("Sending request to OpenAI with model: {}", request.model());
            
            // OpenAI API 요청 생성
            Map<String, Object> requestBody = Map.of(
                "model", request.model(),
                "messages", java.util.List.of(
                    Map.of("role", "user", "content", request.prompt())
                ),
                "max_tokens", request.maxTokens() != null ? request.maxTokens() : 150,
                "temperature", request.temperature() != null ? request.temperature() : 0.8
            );

            String responseJson = webClient
                .post()
                .uri(baseUrl + "/v1/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

            if (responseJson == null) {
                throw new AiResponseException("No response from OpenAI API");
            }

            return parseResponse(responseJson, request.model());

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new AiResponseException("Failed to generate AI response from OpenAI", e);
        }
    }

    @Override
    public boolean isHealthy() {
        if (!isEnabled() || apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        try {
            String response = webClient
                .get()
                .uri(baseUrl + "/v1/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            return response != null && response.contains("gpt");
        } catch (Exception e) {
            log.warn("OpenAI API health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled) && apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * OpenAI API 응답을 파싱합니다.
     */
    private AiResponse parseResponse(String jsonResponse, String model) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
            throw new AiResponseException("No choices in OpenAI response");
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            throw new AiResponseException("No message in OpenAI response");
        }

        JsonNode content = message.get("content");
        if (content == null) {
            throw new AiResponseException("No content in OpenAI response");
        }

        String result = content.asText();
        if (result == null || result.trim().isEmpty()) {
            throw new AiResponseException("Empty content in OpenAI response");
        }

        // 토큰 사용량 파싱
        Integer tokensUsed = null;
        JsonNode usage = root.get("usage");
        if (usage != null) {
            JsonNode totalTokens = usage.get("total_tokens");
            if (totalTokens != null) {
                tokensUsed = totalTokens.asInt();
            }
        }

        return AiResponse.create(result.trim(), model, PROVIDER_NAME, tokensUsed);
    }
}
