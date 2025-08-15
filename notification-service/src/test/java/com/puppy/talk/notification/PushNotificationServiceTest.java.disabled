package com.puppy.talk.service;

import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.push.DeviceTokenIdentity;
import com.puppy.talk.push.DeviceTokenRepository;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.push.PushNotification;
import com.puppy.talk.push.PushNotificationIdentity;
import com.puppy.talk.push.PushNotificationRepository;
import com.puppy.talk.push.PushNotificationSender;
import com.puppy.talk.push.*;
import com.puppy.talk.push.PushNotificationStatus;
import com.puppy.talk.user.UserIdentity;
import com.puppy.talk.notification.PushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushNotificationService 테스트")
class PushNotificationServiceTest {
    
    @Mock
    private PushNotificationRepository pushNotificationRepository;
    
    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    
    @Mock
    private PushNotificationSender pushNotificationSender;
    
    @InjectMocks
    private PushNotificationService pushNotificationService;
    
    private UserIdentity userId;
    private DeviceToken mockDeviceToken;
    private PushNotification mockNotification;
    
    @BeforeEach
    void setUp() {
        userId = UserIdentity.of(1L);
        
        mockDeviceToken = DeviceToken.of(userId, "test-token", "device-123", "android")
            .withIdentity(DeviceTokenIdentity.of(1L));
            
        mockNotification = PushNotification.of(
            userId,
            mockDeviceToken.token(),
            NotificationType.INACTIVITY_MESSAGE,
            "테스트 제목",
            "테스트 메시지",
            "{\"petId\":1}"
        ).withIdentity(PushNotificationIdentity.of(1L));
    }
    
    @Test
    @DisplayName("즉시 푸시 알림 전송 - 성공")
    void sendImmediateNotification_Success() {
        // Given
        String title = "테스트 알림";
        String message = "테스트 메시지";
        String data = "{\"test\":\"data\"}";
        
        when(deviceTokenRepository.findActiveByUserId(userId)).thenReturn(List.of(mockDeviceToken));
        when(pushNotificationRepository.save(any(PushNotification.class))).thenReturn(mockNotification);
        doNothing().when(pushNotificationSender).send(any(PushNotification.class));
        
        // When
        pushNotificationService.sendNotification(
            userId, 
            NotificationType.NEW_MESSAGE, 
            title, 
            message, 
            data
        );
        
        // Then
        verify(deviceTokenRepository).findActiveByUserId(userId);
        verify(pushNotificationRepository, times(2)).save(any(PushNotification.class)); // 초기 저장 + 전송 완료 저장
        verify(pushNotificationSender).send(any(PushNotification.class));
    }
    
    @Test
    @DisplayName("예약 푸시 알림 전송 - 성공")
    void sendScheduledNotification_Success() {
        // Given
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        
        when(deviceTokenRepository.findActiveByUserId(userId)).thenReturn(List.of(mockDeviceToken));
        when(pushNotificationRepository.save(any(PushNotification.class))).thenReturn(mockNotification);
        
        // When
        pushNotificationService.sendNotification(
            userId,
            NotificationType.INACTIVITY_MESSAGE,
            "예약 알림",
            "예약된 메시지",
            null,
            scheduledTime
        );
        
        // Then
        verify(deviceTokenRepository).findActiveByUserId(userId);
        verify(pushNotificationRepository).save(any(PushNotification.class));
        // 예약 알림이므로 즉시 전송되지 않음
        verify(pushNotificationSender, never()).send(any());
    }
    
    @Test
    @DisplayName("활성 토큰이 없을 때 - 무시")
    void sendNotification_NoActiveTokens() {
        // Given
        when(deviceTokenRepository.findActiveByUserId(userId)).thenReturn(List.of());
        
        // When
        pushNotificationService.sendNotification(
            userId,
            NotificationType.NEW_MESSAGE,
            "제목",
            "메시지",
            null
        );
        
        // Then
        verify(deviceTokenRepository).findActiveByUserId(userId);
        verify(pushNotificationRepository, never()).save(any());
        verify(pushNotificationSender, never()).send(any());
    }
    
    @Test
    @DisplayName("여러 디바이스에 알림 전송 - 성공")
    void sendNotificationToMultipleDevices_Success() {
        // Given
        DeviceToken anotherToken = DeviceToken.of(userId, "token-2", "device-456", "ios")
            .withIdentity(DeviceTokenIdentity.of(2L));
            
        when(deviceTokenRepository.findActiveByUserId(userId))
            .thenReturn(List.of(mockDeviceToken, anotherToken));
        when(pushNotificationRepository.save(any(PushNotification.class)))
            .thenReturn(mockNotification);
        doNothing().when(pushNotificationSender).send(any(PushNotification.class));
        
        // When
        pushNotificationService.sendNotification(
            userId,
            NotificationType.NEW_MESSAGE,
            "제목",
            "메시지",
            null
        );
        
        // Then
        verify(deviceTokenRepository).findActiveByUserId(userId);
        verify(pushNotificationRepository, times(4)).save(any(PushNotification.class)); // 2개 디바이스 × (초기 저장 + 전송 완료 저장)
        verify(pushNotificationSender, times(2)).send(any(PushNotification.class));
    }
    
    @Test
    @DisplayName("필수값 검증 - 실패")
    void sendNotification_ValidationFailure() {
        // When & Then
        assertThatThrownBy(() -> pushNotificationService.sendNotification(
            null, NotificationType.NEW_MESSAGE, "제목", "메시지", null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null");
          
        assertThatThrownBy(() -> pushNotificationService.sendNotification(
            userId, null, "제목", "메시지", null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("NotificationType cannot be null");
          
        assertThatThrownBy(() -> pushNotificationService.sendNotification(
            userId, NotificationType.NEW_MESSAGE, null, "메시지", null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Title cannot be null or empty");
          
        assertThatThrownBy(() -> pushNotificationService.sendNotification(
            userId, NotificationType.NEW_MESSAGE, "제목", null, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Message cannot be null or empty");
    }
    
    @Test
    @DisplayName("대기중인 알림 처리 - 성공")
    void processPendingNotifications_Success() {
        // Given
        List<PushNotification> pendingNotifications = List.of(mockNotification);
        when(pushNotificationRepository.findPendingNotifications()).thenReturn(pendingNotifications);
        when(pushNotificationRepository.save(any(PushNotification.class)))
            .thenReturn(mockNotification.markAsSent());
        doNothing().when(pushNotificationSender).send(any(PushNotification.class));
        
        // When
        pushNotificationService.processPendingNotifications();
        
        // Then
        verify(pushNotificationRepository).findPendingNotifications();
        verify(pushNotificationSender).send(mockNotification);
        verify(pushNotificationRepository).save(any(PushNotification.class));
    }
    
    @Test
    @DisplayName("대기중인 알림 없음 - 무시")
    void processPendingNotifications_NoNotifications() {
        // Given
        when(pushNotificationRepository.findPendingNotifications()).thenReturn(List.of());
        
        // When
        pushNotificationService.processPendingNotifications();
        
        // Then
        verify(pushNotificationRepository).findPendingNotifications();
        verify(pushNotificationSender, never()).send(any());
        verify(pushNotificationRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("알림 전송 실패 처리 - 실패")
    void processPendingNotifications_SendFailure() {
        // Given
        List<PushNotification> pendingNotifications = List.of(mockNotification);
        when(pushNotificationRepository.findPendingNotifications()).thenReturn(pendingNotifications);
        when(pushNotificationRepository.save(any(PushNotification.class)))
            .thenReturn(mockNotification.markAsFailed("전송 실패"));
        doThrow(new RuntimeException("전송 실패")).when(pushNotificationSender).send(any());
        
        // When
        pushNotificationService.processPendingNotifications();
        
        // Then
        verify(pushNotificationRepository).findPendingNotifications();
        verify(pushNotificationSender).send(mockNotification);
        verify(pushNotificationRepository).save(argThat(notification -> 
            notification.status() == PushNotificationStatus.FAILED &&
            notification.errorMessage().equals("전송 실패")
        ));
    }
    
    @Test
    @DisplayName("알림 히스토리 조회 - 성공")
    void getNotificationHistory_Success() {
        // Given
        int limit = 10;
        List<PushNotification> notifications = List.of(mockNotification);
        when(pushNotificationRepository.findRecentByUserId(userId, limit)).thenReturn(notifications);
        
        // When
        List<PushNotification> result = pushNotificationService.getNotificationHistory(userId, limit);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockNotification);
        
        verify(pushNotificationRepository).findRecentByUserId(userId, limit);
    }
    
    @Test
    @DisplayName("통계 조회 - 성공")
    void getStatistics_Success() {
        // Given
        when(pushNotificationRepository.count()).thenReturn(100L);
        when(pushNotificationRepository.countByStatus(PushNotificationStatus.PENDING)).thenReturn(5L);
        when(pushNotificationRepository.countByStatus(PushNotificationStatus.SENT)).thenReturn(80L);
        when(pushNotificationRepository.countByStatus(PushNotificationStatus.FAILED)).thenReturn(15L);
        when(pushNotificationRepository.countByStatus(PushNotificationStatus.RECEIVED)).thenReturn(70L);
        
        // When
        PushNotificationService.NotificationStatistics result = pushNotificationService.getStatistics();
        
        // Then
        assertThat(result.totalCount()).isEqualTo(100L);
        assertThat(result.pendingCount()).isEqualTo(5L);
        assertThat(result.sentCount()).isEqualTo(80L);
        assertThat(result.failedCount()).isEqualTo(15L);
        assertThat(result.receivedCount()).isEqualTo(70L);
        
        verify(pushNotificationRepository).count();
        verify(pushNotificationRepository, times(4)).countByStatus(any());
    }
    
    @Test
    @DisplayName("알림 수신 확인 - 성공")
    void markAsReceived_Success() {
        // Given
        Long notificationId = 1L;
        when(pushNotificationRepository.findByIdentity(any(PushNotificationIdentity.class)))
            .thenReturn(Optional.of(mockNotification));
        when(pushNotificationRepository.save(any(PushNotification.class)))
            .thenReturn(mockNotification.markAsReceived());
        
        // When
        pushNotificationService.markAsReceived(notificationId);
        
        // Then
        verify(pushNotificationRepository).findByIdentity(PushNotificationIdentity.of(notificationId));
        verify(pushNotificationRepository).save(argThat(notification ->
            notification.status() == PushNotificationStatus.RECEIVED
        ));
    }
    
    @Test
    @DisplayName("존재하지 않는 알림 수신 확인 - 무시")
    void markAsReceived_NotificationNotFound() {
        // Given
        Long notificationId = 999L;
        when(pushNotificationRepository.findByIdentity(any(PushNotificationIdentity.class)))
            .thenReturn(Optional.empty());
        
        // When
        pushNotificationService.markAsReceived(notificationId);
        
        // Then
        verify(pushNotificationRepository).findByIdentity(PushNotificationIdentity.of(notificationId));
        verify(pushNotificationRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("null ID로 수신 확인 - 실패")
    void markAsReceived_NullId() {
        // When & Then
        assertThatThrownBy(() -> pushNotificationService.markAsReceived(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("NotificationId cannot be null");
            
        verify(pushNotificationRepository, never()).findByIdentity(any());
        verify(pushNotificationRepository, never()).save(any());
    }
}