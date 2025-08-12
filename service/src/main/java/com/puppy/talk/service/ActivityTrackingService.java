package com.puppy.talk.service;

import com.puppy.talk.infrastructure.activity.InactivityNotificationRepository;
import com.puppy.talk.infrastructure.activity.UserActivityRepository;
import com.puppy.talk.model.activity.ActivityType;
import com.puppy.talk.model.activity.InactivityNotification;
import com.puppy.talk.model.activity.UserActivity;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.user.UserIdentity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 사용자 활동 추적 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityTrackingService {

    private final UserActivityRepository userActivityRepository;
    private final InactivityNotificationRepository inactivityNotificationRepository;

    /**
     * 사용자 활동을 기록합니다.
     * 활동 기록 시 비활성 알림 설정도 함께 업데이트합니다.
     */
    @Transactional
    public void trackActivity(UserIdentity userId, ChatRoomIdentity chatRoomId, ActivityType activityType) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (activityType == null) {
            throw new IllegalArgumentException("ActivityType cannot be null");
        }

        LocalDateTime now = LocalDateTime.now();
        
        // 사용자 활동 기록
        UserActivity activity = UserActivity.of(userId, chatRoomId, activityType, now);
        userActivityRepository.save(activity);
        
        // 비활성 알림 설정 업데이트
        updateInactivityNotification(chatRoomId, now);
        
        log.debug("Activity tracked: userId={}, chatRoomId={}, activityType={}, timestamp={}", 
            userId.id(), chatRoomId.id(), activityType, now);
    }

    /**
     * 메시지 전송 활동을 기록합니다.
     */
    @Transactional
    public void trackMessageSent(UserIdentity userId, ChatRoomIdentity chatRoomId) {
        trackActivity(userId, chatRoomId, ActivityType.MESSAGE_SENT);
    }

    /**
     * 메시지 읽음 활동을 기록합니다.
     */
    @Transactional
    public void trackMessageRead(UserIdentity userId, ChatRoomIdentity chatRoomId) {
        trackActivity(userId, chatRoomId, ActivityType.MESSAGE_READ);
    }

    /**
     * 채팅방 열기 활동을 기록합니다.
     */
    @Transactional
    public void trackChatOpened(UserIdentity userId, ChatRoomIdentity chatRoomId) {
        trackActivity(userId, chatRoomId, ActivityType.CHAT_OPENED);
    }

    /**
     * 특정 채팅방의 마지막 활동을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<UserActivity> getLastActivity(ChatRoomIdentity chatRoomId) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        
        return userActivityRepository.findLastActivityByChatRoomId(chatRoomId);
    }

    /**
     * 특정 사용자의 마지막 활동을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<UserActivity> getLastActivity(UserIdentity userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        return userActivityRepository.findLastActivityByUserId(userId);
    }

    /**
     * 비활성 알림 설정을 업데이트합니다.
     * 기존 알림이 있으면 업데이트하고, 없으면 새로 생성합니다.
     * 
     * 비활성 알림 업데이트 실패는 활동 기록에는 영향을 주지 않으므로
     * 예외를 로깅만 하고 전파하지 않습니다.
     */
    private void updateInactivityNotification(ChatRoomIdentity chatRoomId, LocalDateTime lastActivityAt) {
        try {
            Optional<InactivityNotification> existingNotification = 
                inactivityNotificationRepository.findByChatRoomId(chatRoomId);

            if (existingNotification.isPresent()) {
                // 기존 알림 업데이트
                InactivityNotification updatedNotification = existingNotification.get()
                    .updateLastActivity(lastActivityAt);
                inactivityNotificationRepository.save(updatedNotification);
                
                log.debug("Updated inactivity notification for chatRoomId={}, newEligibleTime={}", 
                    chatRoomId.id(), updatedNotification.notificationEligibleAt());
            } else {
                // 새 알림 생성
                InactivityNotification newNotification = InactivityNotification.of(
                    chatRoomId, lastActivityAt);
                inactivityNotificationRepository.save(newNotification);
                
                log.debug("Created new inactivity notification for chatRoomId={}, eligibleTime={}", 
                    chatRoomId.id(), newNotification.notificationEligibleAt());
            }
        } catch (Exception e) {
            log.warn("Failed to update inactivity notification for chatRoomId={}: {}. Activity tracking will continue.", 
                chatRoomId.id(), e.getMessage());
        }
    }
}