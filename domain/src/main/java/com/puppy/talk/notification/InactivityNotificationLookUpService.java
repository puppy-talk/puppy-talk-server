package com.puppy.talk.notification;

import com.puppy.talk.activity.InactivityNotification;
import com.puppy.talk.activity.InactivityNotificationIdentity;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.notification.dto.InactivityNotificationStatistics;
import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 비활성 알림 조회 서비스 인터페이스
 */
public interface InactivityNotificationLookUpService {
    
    /**
     * 알림 대상이 된 비활성 알림들을 처리합니다.
     */
    void processEligibleNotifications();
    
    /**
     * 특정 채팅방의 비활성 알림을 비활성화합니다.
     * 
     * @param chatRoom 채팅방
     */
    void disableNotification(ChatRoom chatRoom);
    
    /**
     * 비활성 알림 상태 통계를 조회합니다.
     * 
     * @return 비활성 알림 통계
     */
    InactivityNotificationStatistics getStatistics();
    
    /**
     * 비활성 사용자에게 알림을 전송합니다.
     * 
     * @param userId 사용자 식별자
     * @param lastActivityTime 마지막 활동 시간
     */
    void sendInactivityNotification(UserIdentity userId, LocalDateTime lastActivityTime);
    
    /**
     * 특정 사용자의 비활성 알림을 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 비활성 알림 목록
     */
    List<InactivityNotification> getInactivityNotifications(UserIdentity userId);
    
    /**
     * 비활성 알림을 읽음 상태로 변경합니다.
     * 
     * @param notificationId 알림 식별자
     */
    void markInactivityNotificationAsRead(InactivityNotificationIdentity notificationId);
}