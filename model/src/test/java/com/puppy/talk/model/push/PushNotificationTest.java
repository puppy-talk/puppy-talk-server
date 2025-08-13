package com.puppy.talk.model.push;

import com.puppy.talk.push.NotificationType;
import com.puppy.talk.push.PushNotification;
import com.puppy.talk.push.PushNotificationIdentity;
import com.puppy.talk.push.PushNotificationStatus;
import com.puppy.talk.user.UserIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PushNotification 도메인 모델 테스트")
class PushNotificationTest {
    
    @Test
    @DisplayName("PushNotification 생성 - 성공")
    void createPushNotification_Success() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String deviceToken = "test-device-token";
        NotificationType notificationType = NotificationType.INACTIVITY_MESSAGE;
        String title = "테스트 제목";
        String message = "테스트 메시지";
        String data = "{\"test\":\"data\"}";
        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(5);
        
        // When
        PushNotification notification = PushNotification.of(
            userId, deviceToken, notificationType, title, message, data, scheduledAt
        );
        
        // Then
        assertThat(notification.identity()).isNull(); // 저장 전에는 null
        assertThat(notification.userId()).isEqualTo(userId);
        assertThat(notification.deviceToken()).isEqualTo(deviceToken);
        assertThat(notification.notificationType()).isEqualTo(notificationType);
        assertThat(notification.title()).isEqualTo(title);
        assertThat(notification.message()).isEqualTo(message);
        assertThat(notification.data()).isEqualTo(data);
        assertThat(notification.status()).isEqualTo(PushNotificationStatus.PENDING);
        assertThat(notification.errorMessage()).isNull();
        assertThat(notification.scheduledAt()).isEqualTo(scheduledAt);
        assertThat(notification.sentAt()).isNull();
        assertThat(notification.createdAt()).isNotNull();
    }
    
    @Test
    @DisplayName("즉시 전송 PushNotification 생성 - 성공")
    void createImmediatePushNotification_Success() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String deviceToken = "test-device-token";
        NotificationType notificationType = NotificationType.NEW_MESSAGE;
        String title = "새 메시지";
        String message = "메시지 내용";
        String data = null;
        
        // When
        PushNotification notification = PushNotification.of(
            userId, deviceToken, notificationType, title, message, data
        );
        
        // Then
        assertThat(notification.scheduledAt()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(1));
        assertThat(notification.status()).isEqualTo(PushNotificationStatus.PENDING);
    }
    
    @Test
    @DisplayName("PushNotification 생성 시 필수값 검증 - 실패")
    void createPushNotification_ValidationFailure() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String deviceToken = "test-token";
        NotificationType notificationType = NotificationType.INACTIVITY_MESSAGE;
        String title = "제목";
        String message = "메시지";
        String data = null;
        LocalDateTime scheduledAt = LocalDateTime.now();
        
        // When & Then
        assertThatThrownBy(() -> PushNotification.of(
            null, deviceToken, notificationType, title, message, data, scheduledAt
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null");
          
        assertThatThrownBy(() -> PushNotification.of(
            userId, null, notificationType, title, message, data, scheduledAt
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("DeviceToken cannot be null or empty");
          
        assertThatThrownBy(() -> PushNotification.of(
            userId, "  ", notificationType, title, message, data, scheduledAt
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("DeviceToken cannot be null or empty");
          
        assertThatThrownBy(() -> PushNotification.of(
            userId, deviceToken, null, title, message, data, scheduledAt
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("NotificationType cannot be null");
          
        assertThatThrownBy(() -> PushNotification.of(
            userId, deviceToken, notificationType, null, message, data, scheduledAt
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Title cannot be null or empty");
          
        assertThatThrownBy(() -> PushNotification.of(
            userId, deviceToken, notificationType, title, null, data, scheduledAt
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Message cannot be null or empty");
    }
    
    @Test
    @DisplayName("문자열 정규화 테스트")
    void stringNormalization_Success() {
        // Given
        UserIdentity userId = UserIdentity.of(1L);
        String deviceToken = "  test-token  ";
        String title = "  테스트 제목  ";
        String message = "  테스트 메시지  ";
        String data = "  {\"test\":\"data\"}  ";
        
        // When
        PushNotification notification = PushNotification.of(
            userId, deviceToken, NotificationType.NEW_MESSAGE, title, message, data
        );
        
        // Then
        assertThat(notification.deviceToken()).isEqualTo("test-token");
        assertThat(notification.title()).isEqualTo("테스트 제목");
        assertThat(notification.message()).isEqualTo("테스트 메시지");
        assertThat(notification.data()).isEqualTo("{\"test\":\"data\"}");
    }
    
    @Test
    @DisplayName("식별자 추가 - 성공")
    void withIdentity_Success() {
        // Given
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE, 
            "제목", "메시지", null
        );
        PushNotificationIdentity identity = PushNotificationIdentity.of(100L);
        
        // When
        PushNotification withIdentity = notification.withIdentity(identity);
        
        // Then
        assertThat(withIdentity.identity()).isEqualTo(identity);
        assertThat(withIdentity.userId()).isEqualTo(notification.userId());
        assertThat(withIdentity.deviceToken()).isEqualTo(notification.deviceToken());
    }
    
    @Test
    @DisplayName("전송 완료로 상태 변경 - 성공")
    void markAsSent_Success() {
        // Given
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null
        );
        LocalDateTime beforeSent = LocalDateTime.now();
        
        // When
        PushNotification sent = notification.markAsSent();
        
        // Then
        assertThat(sent.status()).isEqualTo(PushNotificationStatus.SENT);
        assertThat(sent.sentAt()).isNotNull();
        assertThat(sent.sentAt()).isAfter(beforeSent.minusSeconds(1));
        assertThat(sent.errorMessage()).isNull();
    }
    
    @Test
    @DisplayName("전송 실패로 상태 변경 - 성공")
    void markAsFailed_Success() {
        // Given
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null
        );
        String errorMessage = "전송 실패: 잘못된 토큰";
        LocalDateTime beforeFailed = LocalDateTime.now();
        
        // When
        PushNotification failed = notification.markAsFailed(errorMessage);
        
        // Then
        assertThat(failed.status()).isEqualTo(PushNotificationStatus.FAILED);
        assertThat(failed.errorMessage()).isEqualTo(errorMessage);
        assertThat(failed.sentAt()).isNotNull();
        assertThat(failed.sentAt()).isAfter(beforeFailed.minusSeconds(1));
    }
    
    @Test
    @DisplayName("수신 확인으로 상태 변경 - 성공")
    void markAsReceived_Success() {
        // Given
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null
        ).markAsSent();
        
        // When
        PushNotification received = notification.markAsReceived();
        
        // Then
        assertThat(received.status()).isEqualTo(PushNotificationStatus.RECEIVED);
        assertThat(received.sentAt()).isEqualTo(notification.sentAt()); // 기존 전송 시간 유지
        assertThat(received.errorMessage()).isEqualTo(notification.errorMessage());
    }
    
    @Test
    @DisplayName("전송 준비 상태 확인 - 대기중이고 예약시간 도래")
    void isReadyToSend_PendingAndTimeReached_True() {
        // Given
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null, pastTime
        );
        
        // When
        boolean isReady = notification.isReadyToSend();
        
        // Then
        assertThat(isReady).isTrue();
    }
    
    @Test
    @DisplayName("전송 준비 상태 확인 - 대기중이지만 예약시간 미도래")
    void isReadyToSend_PendingButTimeNotReached_False() {
        // Given
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(10);
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null, futureTime
        );
        
        // When
        boolean isReady = notification.isReadyToSend();
        
        // Then
        assertThat(isReady).isFalse();
    }
    
    @Test
    @DisplayName("전송 준비 상태 확인 - 이미 전송됨")
    void isReadyToSend_AlreadySent_False() {
        // Given
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null, LocalDateTime.now().minusMinutes(1)
        ).markAsSent();
        
        // When
        boolean isReady = notification.isReadyToSend();
        
        // Then
        assertThat(isReady).isFalse();
    }
    
    @Test
    @DisplayName("전송 준비 상태 확인 - 실패 상태")
    void isReadyToSend_Failed_False() {
        // Given
        PushNotification notification = PushNotification.of(
            UserIdentity.of(1L), "token", NotificationType.NEW_MESSAGE,
            "제목", "메시지", null, LocalDateTime.now().minusMinutes(1)
        ).markAsFailed("전송 실패");
        
        // When
        boolean isReady = notification.isReadyToSend();
        
        // Then
        assertThat(isReady).isFalse();
    }
}