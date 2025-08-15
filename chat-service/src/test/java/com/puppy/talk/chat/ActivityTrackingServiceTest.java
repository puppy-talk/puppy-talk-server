package com.puppy.talk.chat;

import com.puppy.talk.activity.ActivityType;
import com.puppy.talk.activity.InactivityNotification;
import com.puppy.talk.activity.InactivityNotificationRepository;
import com.puppy.talk.activity.InactivityNotificationIdentity;
import com.puppy.talk.activity.NotificationStatus;
import com.puppy.talk.activity.UserActivity;
import com.puppy.talk.activity.UserActivityRepository;
import com.puppy.talk.activity.UserActivityIdentity;
import com.puppy.talk.user.UserIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ActivityTrackingService 테스트")
class ActivityTrackingServiceTest {

    private UserActivityRepository userActivityRepository;
    private InactivityNotificationRepository inactivityNotificationRepository;
    private ActivityTrackingService activityTrackingService;

    private UserIdentity userId;
    private ChatRoomIdentity chatRoomId;

    @BeforeEach
    void setUp() {
        // Mock objects 직접 생성
        userActivityRepository = new MockUserActivityRepository();
        inactivityNotificationRepository = new MockInactivityNotificationRepository();
        
        activityTrackingService = new ActivityTrackingService(userActivityRepository, inactivityNotificationRepository);
        
        userId = UserIdentity.of(1L);
        chatRoomId = ChatRoomIdentity.of(1L);
    }

    @Test
    @DisplayName("사용자 활동을 성공적으로 기록한다")
    void trackActivity_Success() {
        // given
        ActivityType activityType = ActivityType.MESSAGE_SENT;
        ((MockInactivityNotificationRepository) inactivityNotificationRepository).setNotification(Optional.empty());
        
        // when
        activityTrackingService.trackActivity(userId, chatRoomId, activityType);
        
        // then
        UserActivity savedActivity = ((MockUserActivityRepository) userActivityRepository).getSavedActivity();
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.userId()).isEqualTo(userId);
        assertThat(savedActivity.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(savedActivity.activityType()).isEqualTo(activityType);
        assertThat(savedActivity.activityAt()).isNotNull();
    }

    @Test
    @DisplayName("활동 기록 시 새로운 비활성 알림을 생성한다")
    void trackActivity_CreateNewInactivityNotification() {
        // given
        ActivityType activityType = ActivityType.CHAT_OPENED;
        ((MockInactivityNotificationRepository) inactivityNotificationRepository).setNotification(Optional.empty());
        
        // when
        activityTrackingService.trackActivity(userId, chatRoomId, activityType);
        
        // then
        verifyUserActivitySaved();
        verifyInactivityNotificationSaved();
    }

    @Test
    @DisplayName("활동 기록 시 기존 비활성 알림을 업데이트한다")
    void trackActivity_UpdateExistingInactivityNotification() {
        // given
        ActivityType activityType = ActivityType.MESSAGE_READ;
        LocalDateTime oldActivityTime = LocalDateTime.now().minusHours(1);
        
        InactivityNotification existingNotification = InactivityNotification.of(chatRoomId, oldActivityTime);
        ((MockInactivityNotificationRepository) inactivityNotificationRepository).setNotification(Optional.of(existingNotification));
        
        // when
        activityTrackingService.trackActivity(userId, chatRoomId, activityType);
        
        // then
        verifyUserActivitySaved();
        verifyInactivityNotificationUpdated();
    }

    @Test
    @DisplayName("메시지 전송 활동을 기록한다")
    void trackMessageSent_Success() {
        // given
        ((MockInactivityNotificationRepository) inactivityNotificationRepository).setNotification(Optional.empty());
        
        // when
        activityTrackingService.trackMessageSent(userId, chatRoomId);
        
        // then
        UserActivity savedActivity = ((MockUserActivityRepository) userActivityRepository).getSavedActivity();
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.activityType()).isEqualTo(ActivityType.MESSAGE_SENT);
    }

    @Test
    @DisplayName("메시지 읽음 활동을 기록한다")
    void trackMessageRead_Success() {
        // given
        ((MockInactivityNotificationRepository) inactivityNotificationRepository).setNotification(Optional.empty());
        
        // when
        activityTrackingService.trackMessageRead(userId, chatRoomId);
        
        // then
        UserActivity savedActivity = ((MockUserActivityRepository) userActivityRepository).getSavedActivity();
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.activityType()).isEqualTo(ActivityType.MESSAGE_READ);
    }

    @Test
    @DisplayName("채팅방 열기 활동을 기록한다")
    void trackChatOpened_Success() {
        // given
        ((MockInactivityNotificationRepository) inactivityNotificationRepository).setNotification(Optional.empty());
        
        // when
        activityTrackingService.trackChatOpened(userId, chatRoomId);
        
        // then
        UserActivity savedActivity = ((MockUserActivityRepository) userActivityRepository).getSavedActivity();
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.activityType()).isEqualTo(ActivityType.CHAT_OPENED);
    }

    @Test
    @DisplayName("특정 채팅방의 마지막 활동을 조회한다")
    void getLastActivity_ByChatRoomId_Success() {
        // given
        LocalDateTime expectedTime = LocalDateTime.now();
        UserActivity expectedActivity = UserActivity.of(userId, chatRoomId, ActivityType.MESSAGE_SENT, expectedTime);
        ((MockUserActivityRepository) userActivityRepository).setLastActivity(Optional.of(expectedActivity));
        
        // when
        Optional<UserActivity> result = activityTrackingService.getLastActivity(chatRoomId);
        
        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedActivity);
    }

    @Test
    @DisplayName("특정 사용자의 마지막 활동을 조회한다")
    void getLastActivity_ByUserId_Success() {
        // given
        LocalDateTime expectedTime = LocalDateTime.now();
        UserActivity expectedActivity = UserActivity.of(userId, chatRoomId, ActivityType.CHAT_OPENED, expectedTime);
        ((MockUserActivityRepository) userActivityRepository).setLastActivity(Optional.of(expectedActivity));
        
        // when
        Optional<UserActivity> result = activityTrackingService.getLastActivity(userId);
        
        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedActivity);
    }

    @Test
    @DisplayName("null 파라미터로 활동 기록 시 예외 발생")
    void trackActivity_WithNullParameters_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> activityTrackingService.trackActivity(null, chatRoomId, ActivityType.MESSAGE_SENT))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserId cannot be null");
        
        assertThatThrownBy(() -> activityTrackingService.trackActivity(userId, null, ActivityType.MESSAGE_SENT))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ChatRoomId cannot be null");
        
        assertThatThrownBy(() -> activityTrackingService.trackActivity(userId, chatRoomId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ActivityType cannot be null");
    }

    @Test
    @DisplayName("null 파라미터로 마지막 활동 조회 시 예외 발생")
    void getLastActivity_WithNullParameters_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> activityTrackingService.getLastActivity((ChatRoomIdentity) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ChatRoomId cannot be null");
        
        assertThatThrownBy(() -> activityTrackingService.getLastActivity((UserIdentity) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserId cannot be null");
    }

    // Helper methods
    private void verifyUserActivitySaved() {
        UserActivity savedActivity = ((MockUserActivityRepository) userActivityRepository).getSavedActivity();
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.userId()).isEqualTo(userId);
        assertThat(savedActivity.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(savedActivity.activityAt()).isNotNull();
    }

    private void verifyInactivityNotificationSaved() {
        InactivityNotification savedNotification = ((MockInactivityNotificationRepository) inactivityNotificationRepository).getSavedNotification();
        assertThat(savedNotification).isNotNull();
        assertThat(savedNotification.chatRoomId()).isEqualTo(chatRoomId);
        assertThat(savedNotification.lastActivityAt()).isNotNull();
        assertThat(savedNotification.notificationEligibleAt()).isNotNull();
    }

    private void verifyInactivityNotificationUpdated() {
        InactivityNotification updatedNotification = ((MockInactivityNotificationRepository) inactivityNotificationRepository).getUpdatedNotification();
        assertThat(updatedNotification).isNotNull();
        assertThat(updatedNotification.chatRoomId()).isEqualTo(chatRoomId);
    }

    // Mock 클래스들
    private static class MockUserActivityRepository implements UserActivityRepository {
        private UserActivity savedActivity;
        private Optional<UserActivity> lastActivity = Optional.empty();
        
        public void setSavedActivity(UserActivity savedActivity) {
            this.savedActivity = savedActivity;
        }
        
        public UserActivity getSavedActivity() {
            return savedActivity;
        }
        
        public void setLastActivity(Optional<UserActivity> lastActivity) {
            this.lastActivity = lastActivity;
        }
        
        @Override
        public UserActivity save(UserActivity activity) {
            this.savedActivity = activity;
            return activity;
        }
        
        @Override
        public Optional<UserActivity> findByIdentity(UserActivityIdentity identity) {
            return Optional.empty();
        }
        
        @Override
        public List<UserActivity> findByUserId(UserIdentity userId) {
            return List.of();
        }
        
        @Override
        public List<UserActivity> findByChatRoomId(ChatRoomIdentity chatRoomId) {
            return List.of();
        }
        
        @Override
        public Optional<UserActivity> findLastActivityByChatRoomId(ChatRoomIdentity chatRoomId) {
            return lastActivity;
        }
        
        @Override
        public Optional<UserActivity> findLastActivityByUserId(UserIdentity userId) {
            return lastActivity;
        }
        
        @Override
        public List<ChatRoomIdentity> findChatRoomsWithLastActivityBefore(LocalDateTime beforeTime) {
            return List.of();
        }
        
        @Override
        public List<UserActivity> findByActivityType(ActivityType activityType) {
            return List.of();
        }
        
        @Override
        public List<UserActivity> findByActivityAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
            return List.of();
        }
    }
    
    private static class MockInactivityNotificationRepository implements InactivityNotificationRepository {
        private Optional<InactivityNotification> notification = Optional.empty();
        private InactivityNotification savedNotification;
        private InactivityNotification updatedNotification;
        
        public void setNotification(Optional<InactivityNotification> notification) {
            this.notification = notification;
        }
        
        public InactivityNotification getSavedNotification() {
            return savedNotification;
        }
        
        public InactivityNotification getUpdatedNotification() {
            return updatedNotification;
        }
        
        @Override
        public Optional<InactivityNotification> findByChatRoomId(ChatRoomIdentity chatRoomId) {
            return notification;
        }
        
        @Override
        public InactivityNotification save(InactivityNotification notification) {
            if (this.notification.isPresent()) {
                this.updatedNotification = notification;
            } else {
                this.savedNotification = notification;
            }
            return notification;
        }
        
        @Override
        public Optional<InactivityNotification> findByIdentity(InactivityNotificationIdentity identity) {
            return Optional.empty();
        }
        
        @Override
        public List<InactivityNotification> findByStatus(NotificationStatus status) {
            return List.of();
        }
        
        @Override
        public List<InactivityNotification> findEligibleNotifications() {
            return List.of();
        }
        
        @Override
        public List<InactivityNotification> findByCreatedAtBefore(LocalDateTime beforeTime) {
            return List.of();
        }
        
        @Override
        public void deleteByChatRoomId(ChatRoomIdentity chatRoomId) {
            // Mock implementation
        }
        
        @Override
        public void delete(InactivityNotification notification) {
            // Mock implementation
        }
        
        @Override
        public long count() {
            return 0L;
        }
        
        @Override
        public long countByStatus(NotificationStatus status) {
            return 0L;
        }
    }
}