package com.puppy.talk.model.push;

import com.puppy.talk.model.user.UserIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DeviceToken 도메인 모델 테스트")
class DeviceTokenTest {
    
    @Test
    @DisplayName("DeviceToken 생성 - 성공")
    void createDeviceToken_Success() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String token = "test-device-token-12345";
        String deviceId = "device-12345";
        String platform = "android";
        
        // When
        DeviceToken deviceToken = DeviceToken.of(userId, token, deviceId, platform);
        
        // Then
        assertThat(deviceToken.identity()).isNull(); // 저장 전에는 null
        assertThat(deviceToken.userId()).isEqualTo(userId);
        assertThat(deviceToken.token()).isEqualTo(token);
        assertThat(deviceToken.deviceId()).isEqualTo(deviceId);
        assertThat(deviceToken.platform()).isEqualTo("android"); // 소문자로 정규화
        assertThat(deviceToken.isActive()).isTrue();
        assertThat(deviceToken.lastUsedAt()).isNotNull();
        assertThat(deviceToken.createdAt()).isNotNull();
        assertThat(deviceToken.lastUsedAt()).isEqualTo(deviceToken.createdAt());
    }
    
    @Test
    @DisplayName("DeviceToken 생성 시 필수값 검증 - 실패")
    void createDeviceToken_ValidationFailure() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String token = "test-token";
        String deviceId = "device-123";
        String platform = "android";
        
        // When & Then
        assertThatThrownBy(() -> DeviceToken.of(null, token, deviceId, platform))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserId cannot be null");
            
        assertThatThrownBy(() -> DeviceToken.of(userId, null, deviceId, platform))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be null or empty");
            
        assertThatThrownBy(() -> DeviceToken.of(userId, "  ", deviceId, platform))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be null or empty");
            
        assertThatThrownBy(() -> DeviceToken.of(userId, token, deviceId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Platform cannot be null or empty");
            
        assertThatThrownBy(() -> DeviceToken.of(userId, token, deviceId, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Platform cannot be null or empty");
    }
    
    @Test
    @DisplayName("문자열 정규화 테스트")
    void stringNormalization_Success() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String token = "  test-token  ";
        String deviceId = "  device-123  ";
        String platform = "  ANDROID  ";
        
        // When
        DeviceToken deviceToken = DeviceToken.of(userId, token, deviceId, platform);
        
        // Then
        assertThat(deviceToken.token()).isEqualTo("test-token");
        assertThat(deviceToken.deviceId()).isEqualTo("device-123");
        assertThat(deviceToken.platform()).isEqualTo("android"); // 소문자로 정규화
    }
    
    @Test
    @DisplayName("deviceId가 null인 경우 처리")
    void createDeviceToken_NullDeviceId_Success() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String token = "test-token";
        String deviceId = null;
        String platform = "web";
        
        // When
        DeviceToken deviceToken = DeviceToken.of(userId, token, deviceId, platform);
        
        // Then
        assertThat(deviceToken.deviceId()).isNull();
        assertThat(deviceToken.platform()).isEqualTo("web");
    }
    
    @Test
    @DisplayName("식별자 추가 - 성공")
    void withIdentity_Success() {
        // Given
        DeviceToken deviceToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "ios"
        );
        DeviceTokenIdentity identity = DeviceTokenIdentity.of(100L);
        
        // When
        DeviceToken withIdentity = deviceToken.withIdentity(identity);
        
        // Then
        assertThat(withIdentity.identity()).isEqualTo(identity);
        assertThat(withIdentity.userId()).isEqualTo(deviceToken.userId());
        assertThat(withIdentity.token()).isEqualTo(deviceToken.token());
        assertThat(withIdentity.deviceId()).isEqualTo(deviceToken.deviceId());
        assertThat(withIdentity.platform()).isEqualTo(deviceToken.platform());
    }
    
    @Test
    @DisplayName("마지막 사용 시간 업데이트 - 성공")
    void updateLastUsed_Success() {
        // Given
        DeviceToken deviceToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "android"
        );
        LocalDateTime originalLastUsed = deviceToken.lastUsedAt();
        
        // When
        DeviceToken updated = deviceToken.updateLastUsed();
        
        // Then
        assertThat(updated.lastUsedAt()).isAfter(originalLastUsed);
        assertThat(updated.userId()).isEqualTo(deviceToken.userId());
        assertThat(updated.token()).isEqualTo(deviceToken.token());
        assertThat(updated.createdAt()).isEqualTo(deviceToken.createdAt());
    }
    
    @Test
    @DisplayName("토큰 활성화 - 성공")
    void activate_Success() {
        // Given
        DeviceToken deviceToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "android"
        ).deactivate(); // 먼저 비활성화
        
        LocalDateTime beforeActivate = LocalDateTime.now();
        
        // When
        DeviceToken activated = deviceToken.activate();
        
        // Then
        assertThat(activated.isActive()).isTrue();
        assertThat(activated.lastUsedAt()).isAfter(beforeActivate.minusSeconds(1));
    }
    
    @Test
    @DisplayName("토큰 비활성화 - 성공")
    void deactivate_Success() {
        // Given
        DeviceToken deviceToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "android"
        );
        
        // When
        DeviceToken deactivated = deviceToken.deactivate();
        
        // Then
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.lastUsedAt()).isEqualTo(deviceToken.lastUsedAt()); // 변경되지 않음
        assertThat(deactivated.userId()).isEqualTo(deviceToken.userId());
        assertThat(deactivated.token()).isEqualTo(deviceToken.token());
    }
    
    @Test
    @DisplayName("iOS 플랫폼 확인 - 성공")
    void isIOS_Success() {
        // Given
        DeviceToken iosToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "ios"
        );
        DeviceToken androidToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-456", "android"
        );
        
        // When & Then
        assertThat(iosToken.isIOS()).isTrue();
        assertThat(androidToken.isIOS()).isFalse();
    }
    
    @Test
    @DisplayName("Android 플랫폼 확인 - 성공")
    void isAndroid_Success() {
        // Given
        DeviceToken androidToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "android"
        );
        DeviceToken iosToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-456", "ios"
        );
        
        // When & Then
        assertThat(androidToken.isAndroid()).isTrue();
        assertThat(iosToken.isAndroid()).isFalse();
    }
    
    @Test
    @DisplayName("Web 플랫폼 확인 - 성공")
    void isWeb_Success() {
        // Given
        DeviceToken webToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "web"
        );
        DeviceToken mobileToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-456", "android"
        );
        
        // When & Then
        assertThat(webToken.isWeb()).isTrue();
        assertThat(mobileToken.isWeb()).isFalse();
    }
    
    @Test
    @DisplayName("플랫폼 대소문자 구분 없이 확인 - 성공")
    void platformCheck_CaseInsensitive_Success() {
        // Given
        DeviceToken upperCaseToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "ANDROID"
        );
        DeviceToken mixedCaseToken = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-456", "IoS"
        );
        
        // When & Then
        assertThat(upperCaseToken.isAndroid()).isTrue();
        assertThat(mixedCaseToken.isIOS()).isTrue();
    }
    
    @Test
    @DisplayName("immutable 객체 특성 확인")
    void immutableObject_Success() {
        // Given
        DeviceToken original = DeviceToken.of(
            UserIdentity.of(1L), "token", "device-123", "android"
        );
        
        // When
        DeviceToken activated = original.activate();
        DeviceToken deactivated = original.deactivate();
        DeviceToken updated = original.updateLastUsed();
        
        // Then
        // 원본 객체는 변경되지 않음
        assertThat(original.isActive()).isTrue();
        
        // 각각 다른 객체
        assertThat(activated).isNotSameAs(original);
        assertThat(deactivated).isNotSameAs(original);
        assertThat(updated).isNotSameAs(original);
        
        // 상태는 각각 다름
        assertThat(activated.isActive()).isTrue();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(updated.lastUsedAt()).isAfter(original.lastUsedAt());
    }
}