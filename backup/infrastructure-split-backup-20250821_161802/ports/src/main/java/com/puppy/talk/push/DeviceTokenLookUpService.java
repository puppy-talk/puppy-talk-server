package com.puppy.talk.push;

import com.puppy.talk.push.dto.DeviceTokenRegistrationCommand;
import com.puppy.talk.user.UserIdentity;

import java.util.List;

/**
 * 디바이스 토큰 조회 서비스 인터페이스
 */
public interface DeviceTokenLookUpService {
    
    /**
     * 디바이스 토큰을 등록합니다.
     * 
     * @param command 디바이스 토큰 등록 명령
     */
    void registerDeviceToken(DeviceTokenRegistrationCommand command);
    
    /**
     * 사용자의 활성 디바이스 토큰 목록을 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 활성 디바이스 토큰 목록
     */
    List<DeviceToken> getActiveTokens(UserIdentity userId);
    
    /**
     * 디바이스 토큰을 비활성화합니다.
     * 
     * @param token 디바이스 토큰
     */
    void deactivateToken(String token);
    
    /**
     * 사용자의 모든 디바이스 토큰을 비활성화합니다.
     * 
     * @param userId 사용자 식별자
     */
    void deactivateAllTokensForUser(UserIdentity userId);
    
    /**
     * 만료된 디바이스 토큰들을 정리합니다.
     */
    void cleanupExpiredTokens();
}