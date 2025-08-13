package com.puppy.talk.service;

import com.puppy.talk.push.DeviceTokenNotFoundException;
import com.puppy.talk.push.DeviceTokenRepository;
import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.push.DeviceTokenIdentity;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.chat.DeviceTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceTokenService 테스트")
class DeviceTokenServiceTest {
    
    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    
    @InjectMocks
    private DeviceTokenService deviceTokenService;
    
    private UserIdentity userId;
    private String deviceToken;
    private String deviceId;
    private String platform;
    private DeviceToken mockDeviceToken;
    
    @BeforeEach
    void setUp() {
        userId = UserIdentity.of(1L);
        deviceToken = "test-device-token-12345";
        deviceId = "device-12345";
        platform = "android";
        
        mockDeviceToken = DeviceToken.of(userId, deviceToken, deviceId, platform)
            .withIdentity(DeviceTokenIdentity.of(1L));
    }
    
    @Test
    @DisplayName("새로운 디바이스 토큰 등록 - 성공")
    void registerNewToken_Success() {
        // Given
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.empty());
        when(deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId)).thenReturn(Optional.empty());
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken);
        
        // When
        DeviceToken result = deviceTokenService.registerOrUpdateToken(userId, deviceToken, deviceId, platform);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.token()).isEqualTo(deviceToken);
        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.platform()).isEqualTo(platform);
        assertThat(result.isActive()).isTrue();
        
        verify(deviceTokenRepository).findByToken(deviceToken);
        verify(deviceTokenRepository).findByUserIdAndDeviceId(userId, deviceId);
        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }
    
    @Test
    @DisplayName("기존 토큰 업데이트 - 성공")
    void updateExistingToken_Success() {
        // Given
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.of(mockDeviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken);
        
        // When
        DeviceToken result = deviceTokenService.registerOrUpdateToken(userId, deviceToken, deviceId, platform);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        
        verify(deviceTokenRepository).findByToken(deviceToken);
        verify(deviceTokenRepository).save(any(DeviceToken.class));
        // 기존 토큰이 있으면 새로 생성하지 않음
        verify(deviceTokenRepository, never()).findByUserIdAndDeviceId(any(), any());
    }
    
    @Test
    @DisplayName("동일 디바이스의 기존 토큰 비활성화 후 새 토큰 등록 - 성공")
    void registerNewTokenWithDeactivatingOldToken_Success() {
        // Given
        DeviceToken oldDeviceToken = DeviceToken.of(userId, "old-token", deviceId, platform)
            .withIdentity(DeviceTokenIdentity.of(2L));
        
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.empty());
        when(deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId)).thenReturn(Optional.of(oldDeviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken);
        
        // When
        DeviceToken result = deviceTokenService.registerOrUpdateToken(userId, deviceToken, deviceId, platform);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(deviceToken);
        
        // 기존 토큰 비활성화 및 새 토큰 저장 확인
        verify(deviceTokenRepository, times(2)).save(any(DeviceToken.class));
    }
    
    @Test
    @DisplayName("토큰 등록 시 필수값 검증 - 실패")
    void registerToken_ValidationFailure() {
        // When & Then
        assertThatThrownBy(() -> deviceTokenService.registerOrUpdateToken(null, deviceToken, deviceId, platform))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserId cannot be null");
            
        assertThatThrownBy(() -> deviceTokenService.registerOrUpdateToken(userId, null, deviceId, platform))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be null or empty");
            
        assertThatThrownBy(() -> deviceTokenService.registerOrUpdateToken(userId, "", deviceId, platform))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be null or empty");
            
        assertThatThrownBy(() -> deviceTokenService.registerOrUpdateToken(userId, deviceToken, deviceId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Platform cannot be null or empty");
    }
    
    @Test
    @DisplayName("사용자의 활성 토큰 조회 - 성공")
    void getActiveTokensByUserId_Success() {
        // Given
        List<DeviceToken> activeTokens = List.of(mockDeviceToken);
        when(deviceTokenRepository.findActiveByUserId(userId)).thenReturn(activeTokens);
        
        // When
        List<DeviceToken> result = deviceTokenService.getActiveTokensByUserId(userId);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockDeviceToken);
        
        verify(deviceTokenRepository).findActiveByUserId(userId);
    }
    
    @Test
    @DisplayName("활성 토큰 존재 확인 - 성공")
    void hasActiveTokens_Success() {
        // Given
        when(deviceTokenRepository.findActiveByUserId(userId)).thenReturn(List.of(mockDeviceToken));
        
        // When
        boolean result = deviceTokenService.hasActiveTokens(userId);
        
        // Then
        assertThat(result).isTrue();
        
        verify(deviceTokenRepository).findActiveByUserId(userId);
    }
    
    @Test
    @DisplayName("활성 토큰 없음 확인 - 성공")
    void hasActiveTokens_NoTokens() {
        // Given
        when(deviceTokenRepository.findActiveByUserId(userId)).thenReturn(List.of());
        
        // When
        boolean result = deviceTokenService.hasActiveTokens(userId);
        
        // Then
        assertThat(result).isFalse();
        
        verify(deviceTokenRepository).findActiveByUserId(userId);
    }
    
    @Test
    @DisplayName("토큰 비활성화 - 성공")
    void deactivateToken_Success() {
        // Given
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.of(mockDeviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken.deactivate());
        
        // When
        deviceTokenService.deactivateToken(deviceToken);
        
        // Then
        verify(deviceTokenRepository).findByToken(deviceToken);
        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }
    
    @Test
    @DisplayName("존재하지 않는 토큰 비활성화 - 실패")
    void deactivateToken_TokenNotFound() {
        // Given
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> deviceTokenService.deactivateToken(deviceToken))
            .isInstanceOf(DeviceTokenNotFoundException.class);
            
        verify(deviceTokenRepository).findByToken(deviceToken);
        verify(deviceTokenRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("모든 토큰 비활성화 - 성공")
    void deactivateAllTokensByUserId_Success() {
        // When
        deviceTokenService.deactivateAllTokensByUserId(userId);
        
        // Then
        verify(deviceTokenRepository).deactivateAllByUserId(userId);
    }
    
    @Test
    @DisplayName("토큰 삭제 - 성공")
    void deleteToken_Success() {
        // When
        deviceTokenService.deleteToken(deviceToken);
        
        // Then
        verify(deviceTokenRepository).deleteByToken(deviceToken);
    }
    
    @Test
    @DisplayName("토큰 사용 시간 업데이트 - 성공")
    void updateTokenUsage_Success() {
        // Given
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.of(mockDeviceToken));
        when(deviceTokenRepository.save(any(DeviceToken.class))).thenReturn(mockDeviceToken.updateLastUsed());
        
        // When
        deviceTokenService.updateTokenUsage(deviceToken);
        
        // Then
        verify(deviceTokenRepository).findByToken(deviceToken);
        verify(deviceTokenRepository).save(any(DeviceToken.class));
    }
    
    @Test
    @DisplayName("존재하지 않는 토큰 사용 시간 업데이트 - 무시")
    void updateTokenUsage_TokenNotFound() {
        // Given
        when(deviceTokenRepository.findByToken(deviceToken)).thenReturn(Optional.empty());
        
        // When
        deviceTokenService.updateTokenUsage(deviceToken);
        
        // Then
        verify(deviceTokenRepository).findByToken(deviceToken);
        verify(deviceTokenRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("null 토큰으로 사용 시간 업데이트 - 무시")
    void updateTokenUsage_NullToken() {
        // When
        deviceTokenService.updateTokenUsage(null);
        
        // Then
        verify(deviceTokenRepository, never()).findByToken(any());
        verify(deviceTokenRepository, never()).save(any());
    }
}