package com.puppy.talk.push.fcm;

import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.push.DeviceTokenNotFoundException;
import com.puppy.talk.push.DeviceTokenRepository;
import com.puppy.talk.push.dto.DeviceTokenRegistrationCommand;
import com.puppy.talk.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * 디바이스 토큰 관리 서비스
 * 
 * 푸시 알림을 위한 디바이스 토큰의 등록, 갱신, 비활성화를 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {
    
    private final DeviceTokenRepository deviceTokenRepository;
    
    /**
     * 디바이스 토큰을 등록하거나 업데이트합니다.
     * 
     * @param command 디바이스 토큰 등록 명령
     * @return 등록/업데이트된 디바이스 토큰
     * @throws IllegalArgumentException 잘못된 매개변수 시
     */
    @Transactional
    public DeviceToken registerOrUpdateToken(DeviceTokenRegistrationCommand command) {
        validateRegistrationCommand(command);
        
        log.debug("Registering device token for user={}, platform={}", 
            command.userId().id(), command.platform());
        
        // 기존 토큰 확인 및 업데이트
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(command.token());
        if (existingToken.isPresent()) {
            return updateExistingToken(existingToken.get());
        }
        
        // 동일 디바이스의 기존 토큰 비활성화
        deactivateOldTokenForSameDevice(command);
        
        // 새 토큰 생성
        return createNewToken(command);
    }
    
    /**
     * 사용자의 활성 디바이스 토큰들을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> getActiveTokensByUserId(UserIdentity userId) {
        Assert.notNull(userId, "UserId cannot be null");
        return deviceTokenRepository.findActiveByUserId(userId);
    }
    
    /**
     * 사용자의 활성 디바이스 토큰이 있는지 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveTokens(UserIdentity userId) {
        return !getActiveTokensByUserId(userId).isEmpty();
    }
    
    /**
     * 디바이스 토큰을 비활성화합니다.
     */
    @Transactional
    public void deactivateToken(String token) {
        Assert.hasText(token, "Token cannot be null or empty");
        
        DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
            .orElseThrow(() -> new DeviceTokenNotFoundException(token));
            
        DeviceToken deactivatedToken = deviceToken.deactivate();
        deviceTokenRepository.save(deactivatedToken);
        
        log.info("Deactivated device token: {}", token);
    }
    
    /**
     * 사용자의 모든 디바이스 토큰을 비활성화합니다.
     */
    @Transactional
    public void deactivateAllTokensByUserId(UserIdentity userId) {
        Assert.notNull(userId, "UserId cannot be null");
        
        deviceTokenRepository.deactivateAllByUserId(userId);
        log.info("Deactivated all device tokens for user: {}", userId.id());
    }
    
    /**
     * 디바이스 토큰을 삭제합니다.
     */
    @Transactional
    public void deleteToken(String token) {
        Assert.hasText(token, "Token cannot be null or empty");
        
        deviceTokenRepository.deleteByToken(token);
        log.info("Deleted device token: {}", token);
    }
    
    /**
     * 토큰 사용 시간을 업데이트합니다.
     */
    @Transactional
    public void updateTokenUsage(String token) {
        if (!org.springframework.util.StringUtils.hasText(token)) {
            return;
        }
        
        deviceTokenRepository.findByToken(token)
            .ifPresent(deviceToken -> {
                DeviceToken updatedToken = deviceToken.updateLastUsed();
                deviceTokenRepository.save(updatedToken);
                log.debug("Updated token usage: {}", token);
            });
    }
    
    // === Private Helper Methods ===
    
    private void validateRegistrationCommand(DeviceTokenRegistrationCommand command) {
        Assert.notNull(command, "DeviceTokenRegistrationCommand cannot be null");
        Assert.notNull(command.userId(), "UserId cannot be null");
        Assert.hasText(command.token(), "Token cannot be null or empty");
        Assert.hasText(command.platform(), "Platform cannot be null or empty");
    }
    
    private DeviceToken updateExistingToken(DeviceToken existingToken) {
        DeviceToken updatedToken = existingToken
            .activate()
            .updateLastUsed();
        
        deviceTokenRepository.save(updatedToken);
        log.debug("Updated existing device token: {}", existingToken.token());
        return updatedToken;
    }
    
    private void deactivateOldTokenForSameDevice(DeviceTokenRegistrationCommand command) {
        if (command.deviceId() == null) {
            return;
        }
        
        deviceTokenRepository.findByUserIdAndDeviceId(command.userId(), command.deviceId())
            .ifPresent(existingDeviceToken -> {
                DeviceToken deactivatedToken = existingDeviceToken.deactivate();
                deviceTokenRepository.save(deactivatedToken);
                log.debug("Deactivated old token for same device: {}", command.deviceId());
            });
    }
    
    private DeviceToken createNewToken(DeviceTokenRegistrationCommand command) {
        DeviceToken newToken = DeviceToken.of(
            command.userId(), 
            command.token(), 
            command.deviceId(), 
            command.platform()
        );
        
        DeviceToken savedToken = deviceTokenRepository.save(newToken);
        log.info("Registered new device token for user={}, platform={}", 
            command.userId().id(), command.platform());
        return savedToken;
    }
}