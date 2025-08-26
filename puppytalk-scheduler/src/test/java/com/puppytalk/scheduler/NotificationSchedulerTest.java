package com.puppytalk.scheduler;

import com.puppytalk.notification.NotificationFacade;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationFacade notificationFacade;

    private NotificationScheduler notificationScheduler;

    @BeforeEach
    void setUp() {
        notificationScheduler = new NotificationScheduler(notificationFacade);
    }

    @Test
    void 비활성_사용자_감지_및_알림_생성_테스트() {
        // Given
        List<Long> inactiveUserIds = List.of(1L, 2L, 3L);
        when(notificationFacade.findInactiveUsersForNotification())
            .thenReturn(inactiveUserIds);

        // When
        notificationScheduler.detectInactiveUsersAndCreateNotifications();

        // Then
        verify(notificationFacade).findInactiveUsersForNotification();
        verify(notificationFacade, times(3)).createInactivityNotification(any());
    }

    @Test
    void 비활성_사용자가_없을_때_테스트() {
        // Given
        when(notificationFacade.findInactiveUsersForNotification())
            .thenReturn(Collections.emptyList());

        // When
        notificationScheduler.detectInactiveUsersAndCreateNotifications();

        // Then
        verify(notificationFacade).findInactiveUsersForNotification();
        verify(notificationFacade, never()).createInactivityNotification(any());
    }

    @Test
    void 대기중인_알림_처리_테스트() {
        // Given
        NotificationResult mockNotification = createMockNotification(1L);
        NotificationListResult mockResult = new NotificationListResult(
            List.of(mockNotification),
            1
        );
        when(notificationFacade.getPendingNotifications(100))
            .thenReturn(mockResult);

        // When
        notificationScheduler.processPendingNotifications();

        // Then
        verify(notificationFacade).getPendingNotifications(100);
        verify(notificationFacade).updateNotificationStatus(any());
    }

    @Test
    void 실패한_알림_재시도_테스트() {
        // Given
        NotificationResult mockNotification = createMockNotification(2L);
        NotificationListResult mockResult = new NotificationListResult(
            List.of(mockNotification),
            1
        );
        when(notificationFacade.getRetryableNotifications(50))
            .thenReturn(mockResult);

        // When
        notificationScheduler.retryFailedNotifications();

        // Then
        verify(notificationFacade).getRetryableNotifications(50);
        verify(notificationFacade).updateNotificationStatus(any());
    }

    @Test
    void 알림_정리_테스트() {
        // Given
        when(notificationFacade.cleanupExpiredNotifications()).thenReturn(5);
        when(notificationFacade.cleanupOldNotifications()).thenReturn(10);

        // When
        notificationScheduler.cleanupNotifications();

        // Then
        verify(notificationFacade).cleanupExpiredNotifications();
        verify(notificationFacade).cleanupOldNotifications();
    }

    private NotificationResult createMockNotification(Long id) {
        return new NotificationResult(
            id,                              // notificationId
            1L,                             // userId
            2L,                             // petId
            3L,                             // chatRoomId
            "INACTIVITY",                   // type
            "테스트 제목",                     // title
            "테스트 내용",                     // content
            "PENDING",                      // status
            java.time.LocalDateTime.now(),  // scheduledAt
            null,                           // sentAt
            null,                           // readAt
            java.time.LocalDateTime.now(),  // createdAt
            0,                              // retryCount
            null,                           // failureReason
            true                            // found
        );
    }
}
