package com.puppytalk.notification;

import com.puppytalk.activity.ActivityDomainService;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.notification.dto.request.NotificationCreateCommand;
import com.puppytalk.notification.dto.request.NotificationStatusUpdateCommand;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResult;
import com.puppytalk.pet.Pet;
import com.puppytalk.pet.PetId;
import com.puppytalk.pet.PetRepository;
import com.puppytalk.user.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional(readOnly = true)
public class NotificationFacade {

    private static final int LAST_ACTIVITY_HOURS = 2; // 마지막 활동 시간
    private final NotificationDomainService notificationDomainService;
    private final ActivityDomainService activityDomainService;
    private final PetRepository petRepository;
    
    public NotificationFacade(
        NotificationDomainService notificationDomainService,
        ActivityDomainService activityDomainService,
        PetRepository petRepository
    ) {
        this.notificationDomainService = notificationDomainService;
        this.activityDomainService = activityDomainService;
        this.petRepository = petRepository;
    }
    
    /**
     * 비활성 사용자 알림 생성
     */
    @Transactional
    public NotificationResult createInactivityNotification(NotificationCreateCommand command) {
        Assert.notNull(command, "NotificationCreateCommand must not be null");
        Assert.notNull(command.petId(), "PetId must not be null for inactivity notification");
        Assert.notNull(command.chatRoomId(), "ChatRoomId must not be null for inactivity notification");
        
        UserId userId = UserId.from(command.userId());
        PetId petId = PetId.from(command.petId());
        ChatRoomId chatRoomId = ChatRoomId.from(command.chatRoomId());
        
        NotificationId notificationId = notificationDomainService.createInactivityNotification(
            userId, petId, chatRoomId, command.title(), command.content()
        );
        
        return NotificationResult.created(notificationId.getValue());
    }
    
    /**
     * 시스템 알림 생성
     */
    @Transactional
    public NotificationResult createSystemNotification(NotificationCreateCommand command) {
        Assert.notNull(command, "NotificationCreateCommand must not be null");
        Assert.notNull(command.userId(), "UserId must not be null");
        Assert.hasText(command.title(), "Title cannot be null or empty");
        Assert.hasText(command.content(), "Content cannot be null or empty");

        UserId userId = UserId.from(command.userId());
        
        NotificationId notificationId = notificationDomainService.createSystemNotification(
            userId, command.title(), command.content()
        );
        
        return NotificationResult.created(notificationId.getValue());
    }
    
    /**
     * 발송 대기 중인 알림 목록 조회 (스케줄러용)
     */
    @Transactional(readOnly = true)
    public NotificationListResult getPendingNotifications(Integer batchSize) {
        int size = batchSize != null ? batchSize : 100; // 기본 100개
        
        List<Notification> notifications = notificationDomainService.getPendingNotifications(size);
        
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
            case "FAILED" -> notificationDomainService.markAsFailed(notificationId, command.failureReason());
            default -> throw new IllegalArgumentException("Unsupported status: " + command.status());
        }
    }
    
    /**
     * 재시도 대상 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public NotificationListResult getRetryableNotifications(Integer batchSize) {
        int size = batchSize != null ? batchSize : 50; // 기본 50개
        
        List<Notification> notifications = notificationDomainService.getRetryableNotifications(size);
        
        return NotificationListResult.from(notifications);
    }
    
    /**
     * 사용자 미읽은 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public NotificationListResult getUnreadNotifications(Long userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId userIdObj = UserId.from(userId);
        List<Notification> notifications = notificationDomainService.getUnreadNotifications(userIdObj);
        
        return NotificationListResult.from(notifications);
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
     * 
     * Application 계층에서 BC 간 오케스트레이션 담당
     */
    @Transactional(readOnly = true)
    public List<Long> findInactiveUsersForNotification() {
        List<UserId> inactiveUsers = activityDomainService.findInactiveUsers(LAST_ACTIVITY_HOURS);
        
        // 알림 가능 사용자 필터링
        List<UserId> targetUserList = notificationDomainService.filterUsersForNotification(inactiveUsers);
        
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
    
    /**
     * 사용자의 활성 반려동물 조회 (Primitive 타입 반환)
     * 
     * @param userId 사용자 ID (Long)
     * @return 첫 번째 활성 반려동물 ID (없으면 null)
     */
    public Long findFirstActivePetByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        List<Pet> activePets = petRepository.findActiveByOwnerId(UserId.from(userId));
        
        if (activePets.isEmpty()) {
            return null;
        }
        
        // 첫 번째 활성 반려동물 반환 (향후 개선: 가장 최근 대화한 반려동물)
        return activePets.get(0).id().getValue();
    }
}