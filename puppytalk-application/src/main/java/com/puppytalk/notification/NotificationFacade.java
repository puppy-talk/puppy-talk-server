package com.puppytalk.notification;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.notification.dto.request.NotificationCreateCommand;
import com.puppytalk.notification.dto.request.NotificationStatusUpdateCommand;
import com.puppytalk.notification.dto.response.NotificationListResult;
import com.puppytalk.notification.dto.response.NotificationResult;
import com.puppytalk.notification.dto.response.NotificationStatsResult;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 파사드
 * 
 * Backend 관점: 안정적인 트랜잭션 관리와 흐름 제어
 */
@Service
@Transactional(readOnly = true)
public class NotificationFacade {
    
    private final NotificationDomainService notificationDomainService;
    
    public NotificationFacade(NotificationDomainService notificationDomainService) {
        this.notificationDomainService = notificationDomainService;
    }
    
    /**
     * 비활성 사용자 알림 생성
     */
    @Transactional
    public NotificationResult createInactivityNotification(NotificationCreateCommand command) {
        Assert.notNull(command, "NotificationCreateCommand must not be null");
        Assert.notNull(command.petId(), "PetId must not be null for inactivity notification");
        Assert.notNull(command.chatRoomId(), "ChatRoomId must not be null for inactivity notification");
        
        UserId userId = UserId.of(command.userId());
        PetId petId = PetId.of(command.petId());
        ChatRoomId chatRoomId = ChatRoomId.of(command.chatRoomId());
        
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

        UserId userId = UserId.of(command.userId());
        
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
        
        NotificationId notificationId = NotificationId.of(command.notificationId());
        
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
        
        UserId userIdObj = UserId.of(userId);
        List<Notification> notifications = notificationDomainService.getUnreadNotifications(userIdObj);
        
        return NotificationListResult.from(notifications);
    }
    
    /**
     * 사용자 미읽은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        UserId userIdObj = UserId.of(userId);
        return notificationDomainService.getUnreadCount(userIdObj);
    }
    
    /**
     * 비활성 사용자 목록 조회 (알림 대상)
     */
    @Transactional(readOnly = true)
    public List<Long> findInactiveUsersForNotification() {
        return notificationDomainService.findInactiveUsersForNotification()
            .stream()
            .map(UserId::getValue)
            .toList();
    }
    
    /**
     * 알림 통계 조회
     */
    @Transactional(readOnly = true)
    public NotificationStatsResult getNotificationStats(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            // 기본값: 최근 7일
            endDate = LocalDateTime.now();
            startDate = endDate.minusDays(7);
        }
        
        NotificationRepository.NotificationStats stats = 
            notificationDomainService.getNotificationStats(startDate, endDate);
        
        return NotificationStatsResult.from(stats);
    }
    
    /**
     * 만료된 알림 정리
     */
    @Transactional
    public int cleanupExpiredNotifications() {
        return notificationDomainService.cleanupExpiredNotifications();
    }
    
    /**
     * 오래된 알림 정리
     */
    @Transactional
    public int cleanupOldNotifications() {
        return notificationDomainService.cleanupOldNotifications();
    }
}