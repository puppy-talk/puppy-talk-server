package com.puppytalk.notification;

import com.puppytalk.user.UserDomainService;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.pet.PetId;
import com.puppytalk.notification.dto.request.NotificationCreateCommand;
import com.puppytalk.notification.dto.request.NotificationStatusUpdateCommand;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResult;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
public class NotificationFacade {

    private static final int LAST_ACTIVITY_HOURS = 2;
    private static final int DEFAULT_BATCH_SIZE = 100;

    private final NotificationDomainService notificationDomainService;
    private final UserDomainService userDomainService;

    public NotificationFacade(
        NotificationDomainService notificationDomainService,
        UserDomainService userDomainService
    ) {
        this.notificationDomainService = notificationDomainService;
        this.userDomainService = userDomainService;
    }

    /**
     * 비활성 사용자 알림 생성
     */
    @Transactional
    public NotificationResult createInactivityNotification(NotificationCreateCommand command) {
        Assert.notNull(command, "NotificationCreateCommand must not be null");
        Assert.notNull(command.petId(), "PetId must not be null for inactivity notification");
        Assert.notNull(command.chatRoomId(),
            "ChatRoomId must not be null for inactivity notification");

        UserId userId = UserId.from(command.userId());
        PetId petId = PetId.from(command.petId());
        ChatRoomId chatRoomId = ChatRoomId.from(command.chatRoomId());

        notificationDomainService.createInactivityNotification(userId, petId, chatRoomId,
            command.title(), command.content()
        );

        return NotificationResult.created(null);
    }


    /**
     * 발송 대기 중인 알림 목록 조회 (스케줄러용)
     */
    @Transactional(readOnly = true)
    public NotificationListResult findPendingNotifications(Integer batchSize) {
        int size = batchSize != null ? batchSize : DEFAULT_BATCH_SIZE;

        List<Notification> notifications = notificationDomainService.findPendingNotifications(size);

        return NotificationListResult.from(notifications);
    }

    /**
     * 알림 상태 업데이트
     */
    @Transactional
    public void updateNotificationStatus(NotificationStatusUpdateCommand command) {
        Assert.notNull(command, "NotificationStatusUpdateCommand must not be null");

        NotificationId notificationId = NotificationId.from(command.notificationId());

        switch (command.status().toUpperCase()) {
            case "SENT" -> notificationDomainService.markAsSent(notificationId);
            case "READ" -> notificationDomainService.markAsRead(notificationId);
            default ->
                throw new IllegalArgumentException("Unsupported status: " + command.status());
        }
    }



    /**
     * 사용자 미읽은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        Assert.notNull(userId, "UserId must not be null");

        UserId userIdObj = UserId.from(userId);
        return notificationDomainService.getUnreadCount(userIdObj);
    }

    /**
     * 비활성 사용자 목록 조회 (알림 대상)
     */
    @Transactional(readOnly = true)
    public List<Long> findInactiveUsersForNotification() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(LAST_ACTIVITY_HOURS);
        List<Long> inactiveUserIds = userDomainService.findInactiveUsers(twoHoursAgo);

        // UserId 리스트로 변환하여 필터링
        List<UserId> inactiveUsers = inactiveUserIds.stream()
            .map(UserId::from)
            .toList();

        // 알림 가능 사용자 필터링
        List<UserId> targetUserList = notificationDomainService.filterUsersForNotification(
            inactiveUsers);

        return targetUserList.stream()
            .map(UserId::getValue)
            .toList();
    }

    /**
     * 만료된 알림 정리
     */
    @Transactional
    public int cleanupExpiredNotifications() {
        return notificationDomainService.cleanupExpiredNotifications();
    }

    /**
     * 오래된 완료 알림 정리
     */
    @Transactional
    public int cleanupOldNotifications() {
        return notificationDomainService.cleanupOldNotifications();
    }

}