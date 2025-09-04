package com.puppytalk.unit.user;

import com.puppytalk.user.User;
import com.puppytalk.user.UserId;
import com.puppytalk.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 엔티티 휴면 상태 테스트")
class UserDormantTest {

    @Test
    @DisplayName("4주 미만 비활성 사용자는 활성 상태")
    void user_active_when_last_activity_within_4_weeks() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recentActivity = now.minusDays(20); // 20일 전 활동
        
        User user = User.of(userId, "testuser", "test@example.com", "password", 
                           now, now, recentActivity, false);

        // when & then
        assertFalse(user.isDormant());
        assertEquals(UserStatus.ACTIVE, user.getCurrentStatus());
        assertTrue(user.canReceiveNotifications());
    }

    @Test
    @DisplayName("정확히 4주(28일) 전 활동한 사용자는 휴면 상태")
    void user_dormant_when_last_activity_exactly_4_weeks_ago() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exactlyDormantDate = now.minusDays(User.DORMANT_DAYS);
        
        User user = User.of(userId, "testuser", "test@example.com", "password", 
                           now, now, exactlyDormantDate, false);

        // when & then
        assertTrue(user.isDormant());
        assertEquals(UserStatus.DORMANT, user.getCurrentStatus());
        assertFalse(user.canReceiveNotifications());
    }

    @Test
    @DisplayName("4주 초과 비활성 사용자는 휴면 상태")
    void user_dormant_when_last_activity_over_4_weeks_ago() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime longTimeAgo = now.minusDays(35); // 35일 전 활동
        
        User user = User.of(userId, "testuser", "test@example.com", "password", 
                           now, now, longTimeAgo, false);

        // when & then
        assertTrue(user.isDormant());
        assertEquals(UserStatus.DORMANT, user.getCurrentStatus());
        assertFalse(user.canReceiveNotifications());
    }

    @Test
    @DisplayName("lastActiveAt이 null인 사용자는 활성 상태")
    void user_active_when_last_active_at_is_null() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        
        User user = User.of(userId, "testuser", "test@example.com", "password", 
                           now, now, null, false);

        // when & then
        assertFalse(user.isDormant());
        assertEquals(UserStatus.ACTIVE, user.getCurrentStatus());
        assertTrue(user.canReceiveNotifications());
    }

    @Test
    @DisplayName("삭제된 사용자는 휴면 여부와 관계없이 DELETED 상태")
    void deleted_user_status_is_deleted_regardless_of_dormancy() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime longTimeAgo = now.minusDays(50); // 50일 전 활동
        
        User user = User.of(userId, "testuser", "test@example.com", "password", 
                           now, now, longTimeAgo, true); // 삭제된 사용자

        // when & then
        assertTrue(user.isDormant()); // 휴면 조건은 만족
        assertEquals(UserStatus.DELETED, user.getCurrentStatus()); // 하지만 삭제 상태
        assertFalse(user.canReceiveNotifications()); // 알림 수신 불가
    }

    @Test
    @DisplayName("활동 시간 업데이트 후 활성 상태로 변경")
    void user_becomes_active_after_updating_last_active_time() {
        // given
        UserId userId = UserId.from(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime longTimeAgo = now.minusDays(35); // 35일 전 활동
        
        User dormantUser = User.of(userId, "testuser", "test@example.com", "password", 
                                 now, now, longTimeAgo, false);
        
        // when - 활동 시간 업데이트
        User activeUser = dormantUser.updateLastActiveTime();

        // then
        assertTrue(dormantUser.isDormant()); // 기존 사용자는 휴면
        assertFalse(activeUser.isDormant()); // 새 사용자는 활성
        assertEquals(UserStatus.ACTIVE, activeUser.getCurrentStatus());
        assertTrue(activeUser.canReceiveNotifications());
    }

    @Test
    @DisplayName("UserStatus.canReceiveNotifications() 동작 확인")
    void user_status_can_receive_notifications() {
        // given & when & then
        assertTrue(UserStatus.ACTIVE.canReceiveNotifications());
        assertFalse(UserStatus.DORMANT.canReceiveNotifications());
        assertFalse(UserStatus.DELETED.canReceiveNotifications());
    }
}