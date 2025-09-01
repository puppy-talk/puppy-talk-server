package com.puppytalk.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.puppytalk.user.UserId;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 JWT 토큰 저장소 구현체
 */
@Component
public class RedisTokenStore implements TokenStore {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisTokenStore.class);
    
    // Redis 키 패턴
    private static final String ACCESS_TOKEN_KEY_PREFIX = "jwt:access:";
    private static final String USER_TOKENS_KEY_PREFIX = "jwt:user:";
    private static final String BLACKLIST_TOKEN_KEY_PREFIX = "jwt:blacklist:";
    
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    
    public RedisTokenStore(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void storeToken(UserId userId, String accessToken, LocalDateTime tokenExpiry) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String clientInfo = getCurrentClientInfo();
            
            // 간단한 토큰 정보 맵을 생성하여 JSON 직렬화 문제 방지
            var tokenInfoMap = new java.util.HashMap<String, Object>();
            tokenInfoMap.put("userId", userId.getValue());
            tokenInfoMap.put("accessToken", accessToken);
            tokenInfoMap.put("tokenExpiry", tokenExpiry.toString());
            tokenInfoMap.put("issuedAt", now.toString());
            tokenInfoMap.put("clientInfo", clientInfo);
            
            String tokenJson = objectMapper.writeValueAsString(tokenInfoMap);
            
            // 액세스 토큰 저장
            RBucket<String> accessBucket = redissonClient.getBucket(ACCESS_TOKEN_KEY_PREFIX + accessToken);
            Duration accessTtl = Duration.between(now, tokenExpiry);
            
            if (!accessTtl.isNegative() && !accessTtl.isZero()) {
                accessBucket.set(tokenJson, accessTtl.toSeconds(), TimeUnit.SECONDS);
                
                // 사용자별 토큰 목록에 추가
                String userTokensKey = USER_TOKENS_KEY_PREFIX + userId.getValue();
                RBucket<String> userTokensBucket = redissonClient.getBucket(userTokensKey + ":" + accessToken);
                userTokensBucket.set(tokenJson, accessTtl.toSeconds(), TimeUnit.SECONDS);
            } else {
                logger.warn("Token TTL is invalid: {} seconds for user: {}", accessTtl.toSeconds(), userId.getValue());
            }
            
            logger.debug("JWT token stored for user: {}", userId.getValue());
        } catch (Exception e) {
            logger.error("Failed to store JWT token for user: {}", userId.getValue(), e);
            throw new TokenStoreException("토큰 저장에 실패했습니다", e);
        }
    }
    
    @Override
    public boolean isTokenActive(String accessToken) {
        try {
            // 블랙리스트 확인
            if (isTokenBlacklisted(accessToken)) {
                return false;
            }
            
            // 액세스 토큰 존재 확인
            RBucket<String> bucket = redissonClient.getBucket(ACCESS_TOKEN_KEY_PREFIX + accessToken);
            String tokenJson = bucket.get();
            
            if (tokenJson == null) {
                return false;
            }
            
            // HashMap으로 역직렬화하여 토큰 정보 확인
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> tokenInfoMap = objectMapper.readValue(tokenJson, java.util.Map.class);
            
            String tokenExpiryStr = (String) tokenInfoMap.get("tokenExpiry");
            LocalDateTime tokenExpiry = LocalDateTime.parse(tokenExpiryStr);
            
            // 토큰 만료 확인
            return LocalDateTime.now().isBefore(tokenExpiry);
        } catch (Exception e) {
            logger.error("Failed to check token status: {}", accessToken.substring(0, 10) + "...", e);
            return false;
        }
    }
    
    
    @Override
    public void invalidateAllTokensForUser(UserId userId) {
        try {
            String pattern = USER_TOKENS_KEY_PREFIX + userId.getValue() + ":*";
            RKeys keys = redissonClient.getKeys();
            
            Iterable<String> userTokenKeys = keys.getKeysByPattern(pattern);
            List<String> keysToDelete = new ArrayList<>();
            
            for (String userTokenKey : userTokenKeys) {
                RBucket<String> bucket = redissonClient.getBucket(userTokenKey);
                String tokenJson = bucket.get();
                
                if (tokenJson != null) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> tokenInfoMap = objectMapper.readValue(tokenJson, java.util.Map.class);
                    
                    String accessToken = (String) tokenInfoMap.get("accessToken");
                    String tokenExpiryStr = (String) tokenInfoMap.get("tokenExpiry");
                    LocalDateTime tokenExpiry = LocalDateTime.parse(tokenExpiryStr);
                    
                    // 토큰을 블랙리스트에 추가
                    addToBlacklist(accessToken, tokenExpiry);
                    
                    // 토큰 저장소에서 제거할 키들 수집
                    keysToDelete.add(ACCESS_TOKEN_KEY_PREFIX + accessToken);
                    keysToDelete.add(userTokenKey);
                }
            }
            
            // 일괄 삭제
            if (!keysToDelete.isEmpty()) {
                keys.delete(keysToDelete.toArray(new String[0]));
            }
            
            logger.info("All tokens invalidated for user: {}", userId.getValue());
        } catch (Exception e) {
            logger.error("Failed to invalidate all tokens for user: {}", userId.getValue(), e);
            throw new TokenStoreException("사용자 토큰 무효화에 실패했습니다", e);
        }
    }
    
    @Override
    public void invalidateToken(String accessToken) {
        try {
            RBucket<String> bucket = redissonClient.getBucket(ACCESS_TOKEN_KEY_PREFIX + accessToken);
            String tokenJson = bucket.get();
            
            if (tokenJson != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> tokenInfoMap = objectMapper.readValue(tokenJson, java.util.Map.class);
                
                String tokenExpiryStr = (String) tokenInfoMap.get("tokenExpiry");
                LocalDateTime tokenExpiry = LocalDateTime.parse(tokenExpiryStr);
                Long userId = Long.valueOf(tokenInfoMap.get("userId").toString());
                
                addToBlacklist(accessToken, tokenExpiry);
                
                // 관련 키들 삭제
                redissonClient.getKeys().delete(
                    ACCESS_TOKEN_KEY_PREFIX + accessToken,
                    USER_TOKENS_KEY_PREFIX + userId + ":" + accessToken
                );
            }
            
            logger.debug("Token invalidated: {}", accessToken.substring(0, 10) + "...");
        } catch (Exception e) {
            logger.error("Failed to invalidate token: {}", accessToken.substring(0, 10) + "...", e);
            throw new TokenStoreException("토큰 무효화에 실패했습니다", e);
        }
    }
    
    
    @Override
    public List<ActiveTokenInfo> getActiveTokensForUser(UserId userId) {
        List<ActiveTokenInfo> activeTokens = new ArrayList<>();
        
        try {
            String pattern = USER_TOKENS_KEY_PREFIX + userId.getValue() + ":*";
            RKeys keys = redissonClient.getKeys();
            
            Iterable<String> userTokenKeys = keys.getKeysByPattern(pattern);
            
            for (String userTokenKey : userTokenKeys) {
                RBucket<String> bucket = redissonClient.getBucket(userTokenKey);
                String tokenJson = bucket.get();
                
                if (tokenJson != null) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> tokenInfoMap = objectMapper.readValue(tokenJson, java.util.Map.class);
                    
                    Long userIdValue = Long.valueOf(tokenInfoMap.get("userId").toString());
                    String accessToken = (String) tokenInfoMap.get("accessToken");
                    String tokenExpiryStr = (String) tokenInfoMap.get("tokenExpiry");
                    String issuedAtStr = (String) tokenInfoMap.get("issuedAt");
                    String clientInfo = (String) tokenInfoMap.get("clientInfo");
                    
                    LocalDateTime tokenExpiry = LocalDateTime.parse(tokenExpiryStr);
                    LocalDateTime issuedAt = LocalDateTime.parse(issuedAtStr);
                    
                    ActiveTokenInfo tokenInfo = new ActiveTokenInfo(
                        UserId.from(userIdValue),
                        accessToken,
                        tokenExpiry,
                        issuedAt,
                        clientInfo
                    );
                    
                    if (tokenInfo.isValid()) {
                        activeTokens.add(tokenInfo);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get active tokens for user: {}", userId.getValue(), e);
        }
        
        return activeTokens;
    }
    
    @Override
    public int cleanupExpiredTokens() {
        // Redis의 TTL 기능으로 자동 정리되므로 블랙리스트만 정리
        try {
            String pattern = BLACKLIST_TOKEN_KEY_PREFIX + "*";
            RKeys keys = redissonClient.getKeys();
            
            Iterable<String> blacklistKeys = keys.getKeysByPattern(pattern);
            List<String> keysToDelete = new ArrayList<>();
            
            for (String key : blacklistKeys) {
                RBucket<String> bucket = redissonClient.getBucket(key);
                if (!bucket.isExists()) {
                    keysToDelete.add(key);
                }
            }
            
            if (!keysToDelete.isEmpty()) {
                keys.delete(keysToDelete.toArray(new String[0]));
                logger.info("Cleaned up {} expired blacklist entries", keysToDelete.size());
                return keysToDelete.size();
            }
            
            return 0;
        } catch (Exception e) {
            logger.error("Failed to cleanup expired tokens", e);
            return 0;
        }
    }
    
    @Override
    public Optional<UserId> getUserIdByToken(String accessToken) {
        try {
            if (!isTokenActive(accessToken)) {
                return Optional.empty();
            }
            
            RBucket<String> bucket = redissonClient.getBucket(ACCESS_TOKEN_KEY_PREFIX + accessToken);
            String tokenJson = bucket.get();
            
            if (tokenJson != null) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> tokenInfoMap = objectMapper.readValue(tokenJson, java.util.Map.class);
                
                Long userIdValue = Long.valueOf(tokenInfoMap.get("userId").toString());
                return Optional.of(UserId.from(userIdValue));
            }
            
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to get user ID by token", e);
            return Optional.empty();
        }
    }
    
    private boolean isTokenBlacklisted(String token) {
        RBucket<String> blacklistBucket = redissonClient.getBucket(BLACKLIST_TOKEN_KEY_PREFIX + token);
        return blacklistBucket.isExists();
    }
    
    private void addToBlacklist(String token, LocalDateTime expiry) {
        try {
            RBucket<String> blacklistBucket = redissonClient.getBucket(BLACKLIST_TOKEN_KEY_PREFIX + token);
            Duration ttl = Duration.between(LocalDateTime.now(), expiry);
            
            if (!ttl.isNegative() && !ttl.isZero()) {
                blacklistBucket.set("blacklisted", ttl.toSeconds(), TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("Failed to add token to blacklist", e);
        }
    }
    
    private String getCurrentClientInfo() {
        // 실제 구현에서는 HttpServletRequest에서 IP, User-Agent 등을 추출
        return "unknown-client";
    }
}