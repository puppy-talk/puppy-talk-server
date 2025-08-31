package com.puppytalk.activity;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.List;

public class ActivityDomainService {

    private final UserActivityRepository userActivityRepository;

    public ActivityDomainService(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    /**
     * 사용자 활동 기록 (채팅방 관련)
     */
    public ActivityId recordActivity(UserId userId, ChatRoomId chatRoomId,
        ActivityType activityType) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }

        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType must not be null");
        }

        LocalDateTime now = LocalDateTime.now();
        UserActivity activity = UserActivity.createActivity(userId, chatRoomId, activityType, now);

        return userActivityRepository.save(activity);
    }

    /**
     * 전역 사용자 활동 기록 (LOGIN)
     */
    public ActivityId recordGlobalActivity(UserId userId, ActivityType activityType) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType must not be null");
        }
        if (!activityType.equals(ActivityType.LOGIN)) {
            throw new IllegalArgumentException(
                "Only LOGIN is allowed for global activities");
        }

        LocalDateTime now = LocalDateTime.now();
        UserActivity activity = UserActivity.createGlobalActivity(userId, activityType, now);

        return userActivityRepository.save(activity);
    }

    /**
     * 비활성 사용자 감지 (시간 커스텀)
     */
    public List<UserId> findInactiveUsers(int inactiveHours) {
        if (inactiveHours <= 0) {
            throw new IllegalArgumentException("Inactive hours must be positive");
        }

        LocalDateTime threshold = LocalDateTime.now().minusHours(inactiveHours);
        return userActivityRepository.findInactiveUserIds(threshold);
    }

}