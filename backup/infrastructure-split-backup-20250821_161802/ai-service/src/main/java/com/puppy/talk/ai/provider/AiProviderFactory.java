package com.puppy.talk.ai.provider;

import com.puppy.talk.ai.AiResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<String, Boolean> healthCheckCache = new ConcurrentHashMap<>();
    
    @Value("${ai.default-provider:gpt-oss}")
    private String defaultProviderName;

    @Value("${ai.fallback-providers:}")
    private List<String> fallbackProviders;
    
    @Value("${ai.health-check-cache-ttl:30000}")
    private long healthCheckCacheTtl;

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
        // 1. 기본 제공업체 확인 (cached health check)
        AiProvider defaultProvider = findHealthyProvider(defaultProviderName);
        if (defaultProvider != null) {
            log.debug("Using default AI provider: {}", defaultProviderName);
            return defaultProvider;
        }

        // 2. 대체 제공업체 시도
        for (String fallbackName : fallbackProviders) {
            AiProvider fallbackProvider = findHealthyProvider(fallbackName);
            if (fallbackProvider != null) {
                log.warn("Using fallback AI provider: {} (default {} is unavailable)", fallbackName, defaultProviderName);
                return fallbackProvider;
            }
        }

        // 3. 활성화된 모든 제공업체 시도
        for (AiProvider provider : providerMap.values()) {
            if (provider.isEnabled() && isHealthyWithCache(provider)) {
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
     * 모든 정상 동작하는 AI 제공업체를 반환합니다 (캐시된 health check 사용).
     */
    public List<AiProvider> getHealthyProviders() {
        return providerMap.values().stream()
            .filter(provider -> provider.isEnabled() && isHealthyWithCache(provider))
            .toList();
    }

    /**
     * 제공업체별 상태 정보를 반환합니다 (캐시된 health check 사용).
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
                        isHealthyWithCache(provider),
                        provider.getSupportedModels()
                    );
                }
            ));
    }
    
    // === Helper Methods for Performance Optimization ===
    
    /**
     * 지정된 이름의 정상 상태 AI 제공업체를 찾습니다.
     */
    private AiProvider findHealthyProvider(String providerName) {
        Optional<AiProvider> provider = getProvider(providerName);
        if (provider.isPresent() && isHealthyWithCache(provider.get())) {
            return provider.get();
        }
        return null;
    }
    
    /**
     * 캐시를 사용하여 health check 를 수행합니다.
     * TTL 내에서는 캐시된 결과를 사용합니다.
     */
    private boolean isHealthyWithCache(AiProvider provider) {
        String cacheKey = provider.getProviderName() + "_health";
        
        // 캐시에서 확인
        Boolean cachedHealth = healthCheckCache.get(cacheKey);
        if (cachedHealth != null) {
            return cachedHealth;
        }
        
        // 실제 health check 수행
        boolean isHealthy = provider.isHealthy();
        
        // 결과를 캐시에 저장 (TTL 설정)
        healthCheckCache.put(cacheKey, isHealthy);
        
        // TTL 후 캐시 제거
        scheduleHealthCheckCacheEviction(cacheKey);
        
        return isHealthy;
    }
    
    /**
     * 지정된 시간 후 health check 캐시를 제거합니다.
     */
    private void scheduleHealthCheckCacheEviction(String cacheKey) {
        // 단순한 TTL 구현 (실제 프로덕션에서는 전용 캐시 솔루션 사용 권장)
        new Thread(() -> {
            try {
                Thread.sleep(healthCheckCacheTtl);
                healthCheckCache.remove(cacheKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * health check 캐시를 수동으로 지욵니다.
     */
    public void clearHealthCheckCache() {
        healthCheckCache.clear();
        log.info("Health check cache cleared for all AI providers");
    }

    /**
     * 제공업체 상태 정보
     */
    public record ProviderStatus(
        String name,
        boolean enabled,
        boolean healthy,
        String[] supportedModels
    ) {
        public static ProviderStatus unavailable(String name) {
            return new ProviderStatus(name, false, false, new String[0]);
        }
    }
}
