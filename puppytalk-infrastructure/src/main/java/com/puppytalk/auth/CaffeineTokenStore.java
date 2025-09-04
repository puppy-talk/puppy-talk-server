package com.puppytalk.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.puppytalk.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Caffeine 기반 TokenStore 구현체
 * 로컬 캐시를 사용한 고성능 토큰 관리
 */
@Component
@ConditionalOnProperty(name = "app.token-store.type", havingValue = "caffeine")
public class CaffeineTokenStore implements TokenStore {
    
    private static final Logger logger = LoggerFactory.getLogger(CaffeineTokenStore.class);
    
    // 토큰 → ActiveTokenInfo 매핑
    private final Cache<String, ActiveTokenInfo> tokenCache;
    
    // 사용자 ID → 토큰 목록 매핑 (빠른 사용자별 조회를 위해)
    private final Map<UserId, List<String>> userTokensMap = new ConcurrentHashMap<>();
    
    public CaffeineTokenStore() {
        this.tokenCache = Caffeine.newBuilder()
            .maximumSize(100_000) // 최대 10만개 토큰
            .expireAfterWrite(Duration.ofHours(24)) // 24시간 후 자동 삭제
            .removalListener((token, tokenInfo, cause) -> {
                if (tokenInfo instanceof ActiveTokenInfo activeToken) {
                    // 사용자별 토큰 맵에서도 제거
                    removeTokenFromUserMap(activeToken.userId(), (String) token);
                    logger.debug("Token removed from cache: {}, cause: {}", token, cause);
                }
            })
            .recordStats() // 통계 수집 활성화
            .build();
    }
    
    @Override
    public void storeToken(UserId userId, String accessToken, LocalDateTime tokenExpiry) {
        
        ActiveTokenInfo tokenInfo = new ActiveTokenInfo(
            userId,
            accessToken,
            tokenExpiry,
            LocalDateTime.now(),
            "caffeine-local" // 로컬 캐시 표시
        );
        
        // 토큰 캐시에 저장
        tokenCache.put(accessToken, tokenInfo);
        
        // 사용자별 토큰 맵에 추가
        userTokensMap.compute(userId, (key, tokens) -> {
            if (tokens == null) {
                return List.of(accessToken);
            } else {
                // 새로운 리스트 생성 (불변성 유지)
                return tokens.stream()
                    .collect(Collectors.toList())
                    .stream()
                    .filter(token -> !token.equals(accessToken)) // 중복 제거
                    .collect(Collectors.toList())
                    .stream()
                    .collect(Collectors.toCollection(() -> {
                        var list = new java.util.ArrayList<>(tokens);
                        list.add(accessToken);
                        return list;
                    }));
            }
        });
        
        logger.debug("Token stored for user: {}", userId.value());
    }
    
    @Override
    public boolean isTokenActive(String accessToken) {
        
        ActiveTokenInfo tokenInfo = tokenCache.getIfPresent(accessToken);
        
        if (tokenInfo == null) {
            return false;
        }
        
        // 만료 시간 확인
        if (tokenInfo.isExpired()) {
            // 만료된 토큰은 캐시에서 제거
            invalidateToken(accessToken);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void invalidateAllTokensForUser(UserId userId) {
        
        List<String> userTokens = userTokensMap.get(userId);
        if (userTokens != null) {
            // 사용자의 모든 토큰을 캐시에서 제거
            userTokens.forEach(tokenCache::invalidate);
            
            // 사용자 토큰 맵에서 제거
            userTokensMap.remove(userId);
            
            logger.debug("All tokens invalidated for user: {}, token count: {}", 
                userId.value(), userTokens.size());
        }
    }
    
    @Override
    public void invalidateToken(String accessToken) {
        
        // 토큰 정보를 먼저 조회해서 사용자 정보를 얻음
        ActiveTokenInfo tokenInfo = tokenCache.getIfPresent(accessToken);
        
        // 토큰 캐시에서 제거
        tokenCache.invalidate(accessToken);
        
        // 사용자별 토큰 맵에서도 제거
        if (tokenInfo != null) {
            removeTokenFromUserMap(tokenInfo.userId(), accessToken);
            logger.debug("Token invalidated: {}", accessToken);
        }
    }
    
    @Override
    public List<ActiveTokenInfo> getActiveTokensForUser(UserId userId) {
        
        List<String> userTokens = userTokensMap.get(userId);
        if (userTokens == null) {
            return List.of();
        }
        
        return userTokens.stream()
            .map(tokenCache::getIfPresent)
            .filter(tokenInfo -> tokenInfo != null && tokenInfo.isValid())
            .collect(Collectors.toList());
    }
    
    @Override
    public int cleanupExpiredTokens() {
        logger.info("Starting expired token cleanup...");
        
        // Caffeine의 자동 만료 기능을 사용하므로, 수동으로 정리할 필요는 적음
        // 통계 정보만 출력
        var stats = tokenCache.stats();
        long totalTokens = tokenCache.estimatedSize();
        
        logger.info("Token cache stats - Size: {}, Hit rate: {:.2f}%, Miss count: {}", 
            totalTokens, stats.hitRate() * 100, stats.missCount());
        
        // 사용자별 토큰 맵 정리 (참조하는 토큰이 없는 사용자 제거)
        int cleanedUserMappings = 0;
        var iterator = userTokensMap.entrySet().iterator();
        
        while (iterator.hasNext()) {
            var entry = iterator.next();
            List<String> validTokens = entry.getValue().stream()
                .filter(token -> tokenCache.getIfPresent(token) != null)
                .collect(Collectors.toList());
            
            if (validTokens.isEmpty()) {
                iterator.remove();
                cleanedUserMappings++;
            } else if (validTokens.size() != entry.getValue().size()) {
                // 유효한 토큰만 남겨둠
                userTokensMap.put(entry.getKey(), validTokens);
            }
        }
        
        logger.info("Cleaned up {} user token mappings", cleanedUserMappings);
        return cleanedUserMappings; // 정리된 사용자 매핑 개수 반환
    }
    
    @Override
    public Optional<UserId> getUserIdByToken(String accessToken) {
        
        ActiveTokenInfo tokenInfo = tokenCache.getIfPresent(accessToken);
        
        if (tokenInfo != null && tokenInfo.isValid()) {
            return Optional.of(tokenInfo.userId());
        }
        
        return Optional.empty();
    }
    
    /**
     * 사용자별 토큰 맵에서 특정 토큰 제거
     */
    private void removeTokenFromUserMap(UserId userId, String accessToken) {
        userTokensMap.computeIfPresent(userId, (key, tokens) -> {
            List<String> updatedTokens = tokens.stream()
                .filter(token -> !token.equals(accessToken))
                .collect(Collectors.toList());
            
            return updatedTokens.isEmpty() ? null : updatedTokens;
        });
    }
}