package com.puppytalk.unit.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.notification.Notification;
import com.puppytalk.notification.NotificationDomainService;
import com.puppytalk.notification.NotificationId;
import com.puppytalk.notification.NotificationRepository;
import com.puppytalk.notification.NotificationStatus;
import com.puppytalk.notification.NotificationType;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationDomainService 단위 테스트")
class NotificationDomainServiceTest {
    
    private NotificationDomainService notificationDomainService;
    private MockNotificationRepository mockRepository;
    
    @BeforeEach
    void setUp() {
        mockRepository = new MockNotificationRepository();
        notificationDomainService = new NotificationDomainService(mockRepository);
    }
    
    @DisplayName("비활성 사용자 알림 생성 - 성공")
    @Test
    void createInactivityNotification_Success() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String title = "버디가 보고싶어해요!";
        String content = "오랜만이에요! 대화해요.";
        
        notificationDomainService.createInactivityNotification(userId, petId, chatRoomId, title, content);

        // then
        assertTrue(mockRepository.isSaveCalled());
        Notification savedNotification = mockRepository.getLastSavedNotification();
        assertNotNull(savedNotification);
        assertEquals(userId, savedNotification.getUserId());
        assertEquals(title.trim(), savedNotification.getTitle());
        assertEquals(content.trim(), savedNotification.getContent());
        assertEquals(NotificationType.INACTIVITY_MESSAGE, savedNotification.getType());
        assertEquals(NotificationStatus.CREATED, savedNotification.getStatus());
    }
    
    @DisplayName("비활성 사용자 알림 생성 - null UserId로 실패")
    @Test
    void createInactivityNotification_NullUserId_ThrowsException() {
        // given
        UserId userId = null;
        PetId petId = PetId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String title = "알림 제목";
        String content = "알림 내용";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationDomainService.createInactivityNotification(userId, petId, chatRoomId, title, content)
        );
        
        assertEquals("UserId must be a valid stored ID", exception.getMessage());
        assertFalse(mockRepository.isSaveCalled());
    }
    
    @DisplayName("비활성 사용자 알림 생성 - 빈 제목으로 실패")
    @Test
    void createInactivityNotification_BlankTitle_ThrowsException() {
        // given
        UserId userId = UserId.from(1L);
        PetId petId = PetId.from(1L);
        ChatRoomId chatRoomId = ChatRoomId.from(1L);
        String title = "   ";
        String content = "알림 내용";
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationDomainService.createInactivityNotification(userId, petId, chatRoomId, title, content)
        );
        
        assertTrue(exception.getMessage().contains("Title"));
        assertFalse(mockRepository.isSaveCalled());
    }
    
    
    @DisplayName("발송 대기 알림 목록 조회 - 성공")
    @Test
    void findPendingNotifications_Success() {
        // given
        int batchSize = 10;
        LocalDateTime now = LocalDateTime.now();
        List<Notification> expectedNotifications = Arrays.asList(
            Notification.of(
                NotificationId.from(1L),
                UserId.from(1L),
                PetId.from(1L),
                ChatRoomId.from(1L),
                NotificationType.INACTIVITY_MESSAGE,
                "알림 1",
                "내용 1",
                NotificationStatus.CREATED,
                now,
                null,
                null,
                now,
                now
            ),
            Notification.of(
                NotificationId.from(2L),
                UserId.from(2L),
                PetId.from(2L),
                ChatRoomId.from(2L),
                NotificationType.INACTIVITY_MESSAGE,
                "알림 2",
                "내용 2",
                NotificationStatus.CREATED,
                now,
                null,
                null,
                now,
                now
            )
        );
        
        mockRepository.setFindPendingNotificationsResult(expectedNotifications);
        
        // when
        List<Notification> result = notificationDomainService.findPendingNotifications(batchSize);
        
        // then
        assertEquals(expectedNotifications, result);
        assertTrue(mockRepository.isFindPendingNotificationsCalled());
        assertEquals(batchSize, mockRepository.getLastFindPendingNotificationsBatchSize());
    }
    
    @DisplayName("발송 대기 알림 목록 조회 - 잘못된 batchSize로 실패")
    @Test
    void findPendingNotifications_InvalidBatchSize_ThrowsException() {
        // given
        int batchSize = 0;
        
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> notificationDomainService.findPendingNotifications(batchSize)
        );
        
        assertTrue(exception.getMessage().contains("Batch size"));
        assertFalse(mockRepository.isFindPendingNotificationsCalled());
    }
    
    @DisplayName("알림 발송 완료 처리")
    @Test
    void markAsSent_Success() {
        // given
        NotificationId notificationId = NotificationId.from(1L);
        
        // when
        notificationDomainService.markAsSent(notificationId);
        
        // then
        assertTrue(mockRepository.isUpdateStatusCalled());
        assertEquals(notificationId, mockRepository.getLastUpdateStatusNotificationId());
        assertEquals(NotificationStatus.SENT, mockRepository.getLastUpdateStatusStatus());
    }
    
    @DisplayName("알림 읽음 처리")
    @Test
    void markAsRead_Success() {
        // given
        NotificationId notificationId = NotificationId.from(1L);
        
        // when
        notificationDomainService.markAsRead(notificationId);
        
        // then
        assertTrue(mockRepository.isUpdateStatusCalled());
        assertEquals(notificationId, mockRepository.getLastUpdateStatusNotificationId());
        assertEquals(NotificationStatus.READ, mockRepository.getLastUpdateStatusStatus());
    }
    
    @DisplayName("생성자 - null 레포지토리로 실패")
    @Test
    void constructor_NullRepository_ThrowsException() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new NotificationDomainService(null)
        );
        
        assertEquals("NotificationRepository must not be null", exception.getMessage());
    }
    
    /**
     * Mock NotificationRepository 구현체
     */
    private static class MockNotificationRepository implements NotificationRepository {
        private boolean saveCalled = false;
        private boolean findPendingNotificationsCalled = false;
        private boolean updateStatusCalled = false;
        
        private Notification lastSavedNotification;
        private int lastFindPendingNotificationsLimit;
        private NotificationId lastUpdateStatusNotificationId;
        private NotificationStatus lastUpdateStatusStatus;
        
        private List<Notification> findPendingNotificationsResult = Arrays.asList();
        
        @Override
        public NotificationId save(Notification notification) {
            saveCalled = true;
            lastSavedNotification = notification;
            return NotificationId.from(1L); // mock return
        }
        
        @Override
        public List<Notification> findPendingNotifications(LocalDateTime now, int limit) {
            findPendingNotificationsCalled = true;
            lastFindPendingNotificationsLimit = limit;
            return findPendingNotificationsResult;
        }
        
        @Override
        public void updateStatus(NotificationId notificationId, NotificationStatus status) {
            updateStatusCalled = true;
            lastUpdateStatusNotificationId = notificationId;
            lastUpdateStatusStatus = status;
        }
        
        @Override
        public boolean existsByUserIdAndTypeAndStatus(UserId userId, NotificationType type, NotificationStatus status) {
            return false; // mock implementation
        }
        
        // 다른 필요한 메서드들 (최소 구현)
        @Override
        public Optional<Notification> findById(NotificationId id) { return Optional.empty(); }
        
        
        
        @Override
        public List<Notification> findByUserIdOrderByCreatedAtDesc(UserId userId, int offset, int limit) { return Arrays.asList(); }
        
        @Override
        public long countUnreadByUserId(UserId userId) { return 0; }
        
        @Override
        public List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status, int limit) { return Arrays.asList(); }
        
        @Override
        public void updateStatusBatch(List<NotificationId> ids, NotificationStatus status) {}
        
        @Override
        public int deleteExpiredNotifications(LocalDateTime cutoffDate) { return 0; }
        
        @Override
        public int deleteCompletedNotificationsOlderThan(LocalDateTime cutoffDate) { return 0; }
        
        @Override
        public NotificationRepository.NotificationStats getNotificationStats(LocalDateTime startDate, LocalDateTime endDate) {
            return new NotificationRepository.NotificationStats(0, 0, 0, 0, 0.0);
        }
        
        @Override
        public long countSentNotificationsByUserAndDate(UserId userId, LocalDateTime date) { return 0; }
        
        // Test helper methods
        public void setFindPendingNotificationsResult(List<Notification> result) {
            this.findPendingNotificationsResult = result;
        }
        
        public boolean isSaveCalled() { return saveCalled; }
        public boolean isFindPendingNotificationsCalled() { return findPendingNotificationsCalled; }
        public boolean isUpdateStatusCalled() { return updateStatusCalled; }
        
        public Notification getLastSavedNotification() { return lastSavedNotification; }
        public int getLastFindPendingNotificationsBatchSize() { return lastFindPendingNotificationsLimit; }
        public NotificationId getLastUpdateStatusNotificationId() { return lastUpdateStatusNotificationId; }
        public NotificationStatus getLastUpdateStatusStatus() { return lastUpdateStatusStatus; }
    }
}