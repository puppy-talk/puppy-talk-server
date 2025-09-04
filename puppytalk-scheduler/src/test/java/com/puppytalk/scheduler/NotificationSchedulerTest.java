package com.puppytalk.scheduler;

import com.puppytalk.NotificationScheduler;
import com.puppytalk.notification.InactivityNotificationFacade;
import com.puppytalk.notification.NotificationFacade;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResult;
import com.puppytalk.pet.PetFacade;
import com.puppytalk.user.UserFacade;
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
    
    @Mock
    private InactivityNotificationFacade inactivityNotificationFacade;
    
    @Mock
    private PetFacade petFacade;
    
    @Mock
    private UserFacade userFacade;

    private NotificationScheduler notificationScheduler;

    @BeforeEach
    void setUp() {
        notificationScheduler = new NotificationScheduler(notificationFacade, inactivityNotificationFacade, petFacade, userFacade);
    }

    @Test
    void 비활성_사용자_감지_및_알림_생성_테스트() {
        // Given
        List<Long> inactiveUserIds = List.of(1L, 2L, 3L);
        when(notificationFacade.findInactiveUsersForNotification())
            .thenReturn(inactiveUserIds);
        when(petFacade.findFirstPetByUserId(any()))
            .thenReturn(1L); // Mock pet ID

        // When
        notificationScheduler.detectInactiveUsersAndCreateNotifications();

        // Then
        verify(notificationFacade).findInactiveUsersForNotification();
        verify(petFacade, times(3)).findFirstPetByUserId(any());
        verify(inactivityNotificationFacade, times(3)).createInactivityNotification(any(), any());
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
        verify(petFacade, never()).findFirstPetByUserId(any());
        verify(inactivityNotificationFacade, never()).createInactivityNotification(any(), any());
    }

    @Test
    void 대기중인_알림_처리_테스트() {
        // Given
        // NotificationFacade의 processPendingNotifications 메서드를 mock
        doNothing().when(notificationFacade).processPendingNotifications(100);

        // When
        notificationScheduler.processPendingNotifications();

        // Then
        verify(notificationFacade).processPendingNotifications(100);
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

    @Test
    void 휴면_사용자_처리_테스트() {
        // Given
        when(userFacade.processDormantUsers()).thenReturn(3);

        // When
        notificationScheduler.processDormantUsers();

        // Then
        verify(userFacade).processDormantUsers();
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
