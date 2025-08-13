package com.puppy.talk.infrastructure.push;

import com.puppy.talk.model.push.DeviceToken;
import com.puppy.talk.model.push.DeviceTokenIdentity;
import com.puppy.talk.model.user.UserIdentity;

import java.util.List;
import java.util.Optional;

/**
 * 디바이스 토큰 저장소 인터페이스
 */
public interface DeviceTokenRepository {
    
    /**
     * 디바이스 토큰을 저장합니다.
     */
    DeviceToken save(DeviceToken deviceToken);
    
    /**
     * 식별자로 디바이스 토큰을 조회합니다.
     */
    Optional<DeviceToken> findByIdentity(DeviceTokenIdentity identity);
    
    /**
     * 토큰 값으로 디바이스 토큰을 조회합니다.
     */
    Optional<DeviceToken> findByToken(String token);
    
    /**
     * 사용자의 활성 디바이스 토큰들을 조회합니다.
     */
    List<DeviceToken> findActiveByUserId(UserIdentity userId);
    
    /**
     * 사용자의 모든 디바이스 토큰들을 조회합니다.
     */
    List<DeviceToken> findByUserId(UserIdentity userId);
    
    /**
     * 사용자와 디바이스 ID로 디바이스 토큰을 조회합니다.
     */
    Optional<DeviceToken> findByUserIdAndDeviceId(UserIdentity userId, String deviceId);
    
    /**
     * 특정 토큰을 삭제합니다.
     */
    void deleteByToken(String token);
    
    /**
     * 사용자의 모든 토큰을 비활성화합니다.
     */
    void deactivateAllByUserId(UserIdentity userId);
    
    /**
     * 특정 플랫폼의 활성 토큰들을 조회합니다.
     */
    List<DeviceToken> findActiveByPlatform(String platform);
}