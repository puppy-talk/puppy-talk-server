package com.puppy.talk.ai.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puppy.talk.ai.AiResponseException;
import com.puppy.talk.ai.provider.AiProvider;
import com.puppy.talk.ai.provider.dto.AiRequest;
import com.puppy.talk.ai.provider.dto.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Anthropic Claude 제공업체 구현
 * 
 * Anthropic API를 통해 Claude 모델과 통신합니다.
 */
@Slf4j
@Component
public class ClaudeProvider implements AiProvider {

    private static final String PROVIDER_NAME = "claude";
    private static final String[] SUPPORTED_MODELS = {
        "claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022",
        "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"
    };

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.claude.api-key:}")
    private String apiKey;

    @Value("${ai.providers.claude.base-url:https://api.anthropic.com}")
    private String baseUrl;

    @Value("${ai.providers.claude.timeout:30}")
    private Integer timeoutSeconds;

    @Value("${ai.providers.claude.enabled:false}")
    private Boolean enabled;

    public ClaudeProvider() {
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
            throw new AiResponseException("Claude provider is disabled");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AiResponseException("Claude API key is not configured");
        }

        try {
            log.debug("Sending request to Claude with model: {}", request.model());
            
            // Claude API 요청 생성 (Messages API 사용)
            Map<String, Object> requestBody = Map.of(
                "model", request.model(),
                "max_tokens", request.maxTokens() != null ? request.maxTokens() : 150,
                "temperature", request.temperature() != null ? request.temperature() : 0.8,
                "messages", java.util.List.of(
                    Map.of("role", "user", "content", request.prompt())
                )
            );

            String responseJson = webClient
                .post()
                .uri(baseUrl + "/v1/messages")
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

            if (responseJson == null) {
                throw new AiResponseException("No response from Claude API");
            }

            return parseResponse(responseJson, request.model());

        } catch (Exception e) {
            log.error("Claude API call failed: {}", e.getMessage(), e);
            throw new AiResponseException("Failed to generate AI response from Claude", e);
        }
    }

    @Override
    public boolean isHealthy() {
        if (!isEnabled() || apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        try {
            // Claude API는 별도의 health check 엔드포인트가 없으므로
            // 간단한 메시지로 테스트
            Map<String, Object> testRequest = Map.of(
                "model", "claude-3-haiku-20240307",
                "max_tokens", 5,
                "messages", java.util.List.of(
                    Map.of("role", "user", "content", "Hi")
                )
            );

            String response = webClient
                .post()
                .uri(baseUrl + "/v1/messages")
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(testRequest)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            return response != null && response.contains("content");
        } catch (Exception e) {
            log.warn("Claude API health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled) && apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Claude API 응답을 파싱합니다.
     */
    private AiResponse parseResponse(String jsonResponse, String model) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        JsonNode content = root.get("content");
        if (content == null || !content.isArray() || content.size() == 0) {
            throw new AiResponseException("No content in Claude response");
        }

        JsonNode firstContent = content.get(0);
        if (firstContent == null) {
            throw new AiResponseException("No first content in Claude response");
        }

        JsonNode text = firstContent.get("text");
        if (text == null) {
            throw new AiResponseException("No text in Claude response");
        }

        String result = text.asText();
        if (result == null || result.trim().isEmpty()) {
            throw new AiResponseException("Empty text in Claude response");
        }

        // 토큰 사용량 파싱
        Integer tokensUsed = null;
        JsonNode usage = root.get("usage");
        if (usage != null) {
            JsonNode inputTokens = usage.get("input_tokens");
            JsonNode outputTokens = usage.get("output_tokens");
            if (inputTokens != null && outputTokens != null) {
                tokensUsed = inputTokens.asInt() + outputTokens.asInt();
            }
        }

        return AiResponse.of(result.trim(), model, PROVIDER_NAME, tokensUsed);
    }
}
