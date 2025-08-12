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
 * gpt-oss 로컬 모델 제공업체 구현
 * 
 * Ollama 서버를 통해 gpt-oss 모델과 통신합니다.
 */
@Slf4j
@Component
public class GptOssProvider implements AiProvider {

    private static final String PROVIDER_NAME = "gpt-oss";
    private static final String[] SUPPORTED_MODELS = {"gpt-oss:20b", "gpt-oss:120b"};

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.gpt-oss.server-url:http://localhost:11434}")
    private String serverUrl;

    @Value("${ai.providers.gpt-oss.timeout:30}")
    private Integer timeoutSeconds;

    @Value("${ai.providers.gpt-oss.enabled:false}")
    private Boolean enabled;

    public GptOssProvider() {
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
            throw new AiResponseException("gpt-oss provider is disabled");
        }

        try {
            log.debug("Sending request to gpt-oss at: {} with model: {}", serverUrl, request.model());
            
            // Chat Completions API 형식의 요청 생성
            Map<String, Object> requestBody = Map.of(
                "model", request.model(),
                "messages", java.util.List.of(
                    Map.of("role", "user", "content", request.prompt())
                ),
                "max_tokens", request.maxTokens() != null ? request.maxTokens() : 150,
                "temperature", request.temperature() != null ? request.temperature() : 0.8,
                "stream", false
            );

            String responseJson = webClient
                .post()
                .uri(serverUrl + "/v1/chat/completions")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

            if (responseJson == null) {
                throw new AiResponseException("No response from gpt-oss server");
            }

            return parseResponse(responseJson, request.model());

        } catch (Exception e) {
            log.error("gpt-oss API call failed: {}", e.getMessage(), e);
            throw new AiResponseException("Failed to generate AI response from gpt-oss", e);
        }
    }

    @Override
    public boolean isHealthy() {
        if (!isEnabled()) {
            return false;
        }

        try {
            String response = webClient
                .get()
                .uri(serverUrl + "/v1/models")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            return response != null && response.contains("gpt-oss");
        } catch (Exception e) {
            log.warn("gpt-oss server health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * gpt-oss 서버 응답을 파싱합니다.
     */
    private AiResponse parseResponse(String jsonResponse, String model) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.size() == 0) {
            throw new AiResponseException("No choices in gpt-oss response");
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            throw new AiResponseException("No message in gpt-oss response");
        }

        JsonNode content = message.get("content");
        if (content == null) {
            throw new AiResponseException("No content in gpt-oss response");
        }

        String result = content.asText();
        if (result == null || result.trim().isEmpty()) {
            throw new AiResponseException("Empty content in gpt-oss response");
        }

        // 토큰 사용량 파싱 (있는 경우)
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
