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
 * Google Gemini 제공업체 구현
 * 
 * Google AI Studio API를 통해 Gemini 모델과 통신합니다.
 */
@Slf4j
@Component
public class GeminiProvider implements AiProvider {

    private static final String PROVIDER_NAME = "gemini";
    private static final String[] SUPPORTED_MODELS = {
        "gemini-1.5-pro", "gemini-1.5-flash", "gemini-1.0-pro"
    };

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.providers.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Value("${ai.providers.gemini.timeout:30}")
    private Integer timeoutSeconds;

    @Value("${ai.providers.gemini.enabled:false}")
    private Boolean enabled;

    public GeminiProvider() {
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
            throw new AiResponseException("Gemini provider is disabled");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AiResponseException("Gemini API key is not configured");
        }

        try {
            log.debug("Sending request to Gemini with model: {}", request.model());
            
            // Gemini API 요청 생성
            Map<String, Object> requestBody = Map.of(
                "contents", java.util.List.of(
                    Map.of("parts", java.util.List.of(
                        Map.of("text", request.prompt())
                    ))
                ),
                "generationConfig", Map.of(
                    "maxOutputTokens", request.maxTokens() != null ? request.maxTokens() : 150,
                    "temperature", request.temperature() != null ? request.temperature() : 0.8
                )
            );

            String responseJson = webClient
                .post()
                .uri(baseUrl + "/v1beta/models/" + request.model() + ":generateContent?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

            if (responseJson == null) {
                throw new AiResponseException("No response from Gemini API");
            }

            return parseResponse(responseJson, request.model());

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new AiResponseException("Failed to generate AI response from Gemini", e);
        }
    }

    @Override
    public boolean isHealthy() {
        if (!isEnabled() || apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        try {
            // Gemini의 models 엔드포인트로 health check
            String response = webClient
                .get()
                .uri(baseUrl + "/v1beta/models?key=" + apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            
            return response != null && response.contains("gemini");
        } catch (Exception e) {
            log.warn("Gemini API health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled) && apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * Gemini API 응답을 파싱합니다.
     */
    private AiResponse parseResponse(String jsonResponse, String model) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        
        String result = extractTextFromResponse(root);

        if (result == null || result.trim().isEmpty()) {
            throw new AiResponseException("Empty text in Gemini response");
        }

        // 토큰 사용량 파싱
        Integer tokensUsed = null;
        JsonNode usageMetadata = root.get("usageMetadata");
        if (usageMetadata != null) {
            JsonNode totalTokenCount = usageMetadata.get("totalTokenCount");
            if (totalTokenCount != null) {
                tokensUsed = totalTokenCount.asInt();
            }
        }

        return AiResponse.of(result.trim(), model, PROVIDER_NAME, tokensUsed);
    }

    private String extractTextFromResponse(JsonNode root) throws AiResponseException {
        JsonNode candidates = root.get("candidates");
        if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
            throw new AiResponseException("No candidates in Gemini response");
        }
        
        JsonNode content = candidates.get(0).get("content");
        if (content == null) {
            throw new AiResponseException("No content in Gemini response");
        }
        
        JsonNode parts = content.get("parts");
        if (parts == null || !parts.isArray() || parts.size() == 0) {
            throw new AiResponseException("No parts in Gemini response");
        }
        
        JsonNode text = parts.get(0).get("text");
        if (text == null) {
            throw new AiResponseException("No text in Gemini response");
        }
        
        return text.asText();
    }
}
