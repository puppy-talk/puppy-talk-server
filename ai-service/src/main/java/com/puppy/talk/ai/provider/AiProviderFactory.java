package com.puppy.talk.ai.provider;

import com.puppy.talk.ai.AiResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 제공업체 팩토리
 * 
 * 설정에 따라 적절한 AI 제공업체를 선택하고 관리합니다.
 */
@Slf4j
@Component
public class AiProviderFactory {

    private final Map<String, AiProvider> providerMap;
    
    @Value("${ai.default-provider:gpt-oss}")
    private String defaultProviderName;

    @Value("${ai.fallback-providers:}")
    private List<String> fallbackProviders;

    public AiProviderFactory(List<AiProvider> providers) {
        this.providerMap = providers.stream()
            .collect(Collectors.toMap(
                AiProvider::getProviderName,
                Function.identity()
            ));
        
        log.info("Initialized AI providers: {}", providerMap.keySet());
    }

    /**
     * 기본 AI 제공업체를 반환합니다.
     */
    public AiProvider getDefaultProvider() {
        return getProvider(defaultProviderName)
            .orElseThrow(() -> new AiResponseException("Default AI provider not found: " + defaultProviderName));
    }

    /**
     * 지정된 이름의 AI 제공업체를 반환합니다.
     */
    public Optional<AiProvider> getProvider(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        AiProvider provider = providerMap.get(providerName.toLowerCase());
        if (provider != null && provider.isEnabled()) {
            return Optional.of(provider);
        }
        
        return Optional.empty();
    }

    /**
     * 사용 가능한 AI 제공업체를 반환합니다 (기본 제공업체 우선).
     * 기본 제공업체를 사용할 수 없는 경우 대체 제공업체를 시도합니다.
     */
    public AiProvider getAvailableProvider() {
        // 기본 제공업체 확인
        Optional<AiProvider> defaultProvider = getProvider(defaultProviderName);
        if (defaultProvider.isPresent() && defaultProvider.get().isHealthy()) {
            log.debug("Using default AI provider: {}", defaultProviderName);
            return defaultProvider.get();
        }

        // 대체 제공업체 시도
        for (String fallbackName : fallbackProviders) {
            Optional<AiProvider> fallbackProvider = getProvider(fallbackName);
            if (fallbackProvider.isPresent() && fallbackProvider.get().isHealthy()) {
                log.warn("Using fallback AI provider: {} (default {} is unavailable)", fallbackName, defaultProviderName);
                return fallbackProvider.get();
            }
        }

        // 활성화된 모든 제공업체 시도
        for (AiProvider provider : providerMap.values()) {
            if (provider.isEnabled() && provider.isHealthy()) {
                log.warn("Using available AI provider: {} (default {} and fallbacks are unavailable)", 
                    provider.getProviderName(), defaultProviderName);
                return provider;
            }
        }

        throw new AiResponseException("No available AI providers found");
    }

    /**
     * 모든 활성화된 AI 제공업체를 반환합니다.
     */
    public List<AiProvider> getEnabledProviders() {
        return providerMap.values().stream()
            .filter(AiProvider::isEnabled)
            .collect(Collectors.toList());
    }

    /**
     * 모든 정상 동작하는 AI 제공업체를 반환합니다.
     */
    public List<AiProvider> getHealthyProviders() {
        return providerMap.values().stream()
            .filter(provider -> provider.isEnabled() && provider.isHealthy())
            .collect(Collectors.toList());
    }

    /**
     * 제공업체별 상태 정보를 반환합니다.
     */
    public Map<String, ProviderStatus> getProvidersStatus() {
        return providerMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    AiProvider provider = entry.getValue();
                    return new ProviderStatus(
                        provider.getProviderName(),
                        provider.isEnabled(),
                        provider.isHealthy(),
                        provider.getSupportedModels()
                    );
                }
            ));
    }

    /**
     * 제공업체 상태 정보
     */
    public record ProviderStatus(
        String name,
        boolean enabled,
        boolean healthy,
        String[] supportedModels
    ) {}
}
