package com.puppy.talk.chat;

import com.puppy.talk.push.DeviceTokenNotFoundException;
import com.puppy.talk.push.DeviceTokenRepository;
import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.push.command.DeviceTokenRegistrationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 디바이스 토큰 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenService {
    
    private final DeviceTokenRepository deviceTokenRepository;
    
    /**
     * 디바이스 토큰을 등록하거나 업데이트합니다.
     */
    @Transactional
    public DeviceToken registerOrUpdateToken(DeviceTokenRegistrationCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (command.token() == null || command.token().trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (command.platform() == null || command.platform().trim().isEmpty()) {
            throw new IllegalArgumentException("Platform cannot be null or empty");
        }
        
        log.debug("Registering device token for user={}, platform={}", command.userId().id(), command.platform());
        
        // 기존 토큰 확인
        Optional<DeviceToken> existingToken = deviceTokenRepository.findByToken(command.token());
        
        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 활성화하고 마지막 사용 시간 업데이트
            DeviceToken updatedToken = existingToken.get()
                .activate()
                .updateLastUsed();
            
            deviceTokenRepository.save(updatedToken);
            
            log.debug("Updated existing device token: {}", command.token());
            return updatedToken;
        }
        
        // 동일한 사용자와 디바이스 ID로 등록된 토큰이 있는지 확인
        if (command.deviceId() != null) {
            Optional<DeviceToken> existingDeviceToken = 
                deviceTokenRepository.findByUserIdAndDeviceId(command.userId(), command.deviceId());
                
            if (existingDeviceToken.isPresent()) {
                // 기존 디바이스의 토큰을 비활성화
                DeviceToken deactivatedToken = existingDeviceToken.get().deactivate();
                deviceTokenRepository.save(deactivatedToken);
                
                log.debug("Deactivated old token for same device: {}", command.deviceId());
            }
        }
        
        // 새 토큰 생성
        DeviceToken newToken = DeviceToken.of(command.userId(), command.token(), command.deviceId(), command.platform());
        DeviceToken savedToken = deviceTokenRepository.save(newToken);
        
        log.info("Registered new device token for user={}, platform={}", command.userId().id(), command.platform());
        return savedToken;
    }
    
    /**
     * 사용자의 활성 디바이스 토큰들을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<DeviceToken> getActiveTokensByUserId(UserIdentity userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        return deviceTokenRepository.findActiveByUserId(userId);
    }
    
    /**
     * 사용자의 활성 디바이스 토큰이 있는지 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveTokens(UserIdentity userId) {
        List<DeviceToken> activeTokens = getActiveTokensByUserId(userId);
        return !activeTokens.isEmpty();
    }
    
    /**
     * 디바이스 토큰을 비활성화합니다.
     */
    @Transactional
    public void deactivateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findByToken(token);
        
        if (deviceToken.isPresent()) {
            DeviceToken deactivatedToken = deviceToken.get().deactivate();
            deviceTokenRepository.save(deactivatedToken);
            
            log.info("Deactivated device token: {}", token);
        } else {
            log.warn("Token not found for deactivation: {}", token);
            throw new DeviceTokenNotFoundException(token);
        }
    }
    
    /**
     * 사용자의 모든 디바이스 토큰을 비활성화합니다.
     */
    @Transactional
    public void deactivateAllTokensByUserId(UserIdentity userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        deviceTokenRepository.deactivateAllByUserId(userId);
        
        log.info("Deactivated all device tokens for user: {}", userId.id());
    }
    
    /**
     * 디바이스 토큰을 삭제합니다.
     */
    @Transactional
    public void deleteToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        
        deviceTokenRepository.deleteByToken(token);
        
        log.info("Deleted device token: {}", token);
    }
    
    /**
     * 토큰 사용 시간을 업데이트합니다.
     */
    @Transactional
    public void updateTokenUsage(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }
        
        Optional<DeviceToken> deviceToken = deviceTokenRepository.findByToken(token);
        
        if (deviceToken.isPresent()) {
            DeviceToken updatedToken = deviceToken.get().updateLastUsed();
            deviceTokenRepository.save(updatedToken);
            
            log.debug("Updated token usage: {}", token);
        }
    }
}