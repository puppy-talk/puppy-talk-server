package com.puppy.talk.push;

import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;

/**
 * 사용자 디바이스 토큰 도메인 모델
 */
public record DeviceToken(
    DeviceTokenIdentity identity,
    UserIdentity userId,
    String token,
    String deviceId,
    String platform, // "ios", "android", "web"
    boolean isActive,
    LocalDateTime lastUsedAt,
    LocalDateTime createdAt
) {
    
    public DeviceToken {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (platform == null || platform.trim().isEmpty()) {
            throw new IllegalArgumentException("Platform cannot be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
        
        // normalize strings
        token = token.trim();
        platform = platform.toLowerCase().trim();
        if (deviceId != null) {
            deviceId = deviceId.trim();
        }
        if (lastUsedAt == null) {
            lastUsedAt = createdAt;
        }
    }
    
    /**
     * 새로운 디바이스 토큰을 생성합니다.
     */
    public static DeviceToken of(
        UserIdentity userId,
        String token,
        String deviceId,
        String platform
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new DeviceToken(
            null, // identity는 저장 시 생성됨
            userId,
            token,
            deviceId,
            platform,
            true,
            now,
            now
        );
    }
    
    /**
     * 식별자를 포함한 새로운 DeviceToken을 생성합니다.
     */
    public DeviceToken withIdentity(DeviceTokenIdentity identity) {
        return new DeviceToken(
            identity,
            this.userId,
            this.token,
            this.deviceId,
            this.platform,
            this.isActive,
            this.lastUsedAt,
            this.createdAt
        );
    }
    
    /**
     * 토큰 사용 시간을 업데이트합니다.
     */
    public DeviceToken updateLastUsed() {
        return new DeviceToken(
            this.identity,
            this.userId,
            this.token,
            this.deviceId,
            this.platform,
            this.isActive,
            LocalDateTime.now(),
            this.createdAt
        );
    }
    
    /**
     * 토큰을 활성화합니다.
     */
    public DeviceToken activate() {
        return new DeviceToken(
            this.identity,
            this.userId,
            this.token,
            this.deviceId,
            this.platform,
            true,
            LocalDateTime.now(),
            this.createdAt
        );
    }
    
    /**
     * 토큰을 비활성화합니다.
     */
    public DeviceToken deactivate() {
        return new DeviceToken(
            this.identity,
            this.userId,
            this.token,
            this.deviceId,
            this.platform,
            false,
            this.lastUsedAt,
            this.createdAt
        );
    }
    
    /**
     * 토큰이 iOS 플랫폼인지 확인합니다.
     */
    public boolean isIOS() {
        return "ios".equals(platform);
    }
    
    /**
     * 토큰이 Android 플랫폼인지 확인합니다.
     */
    public boolean isAndroid() {
        return "android".equals(platform);
    }
    
    /**
     * 토큰이 웹 플랫폼인지 확인합니다.
     */
    public boolean isWeb() {
        return "web".equals(platform);
    }
}