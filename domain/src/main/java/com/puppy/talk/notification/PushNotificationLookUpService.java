package com.puppy.talk.notification;

import com.puppy.talk.notification.dto.NotificationStatistics;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.push.PushNotification;
import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 푸시 알림 조회 서비스 인터페이스
 */
public interface PushNotificationLookUpService {
    
    /**
     * 사용자에게 즉시 푸시 알림을 전송합니다.
     * 
     * @param userId 사용자 식별자
     * @param notificationType 알림 타입
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param data 알림 데이터
     */
    void sendNotification(
        UserIdentity userId,
        NotificationType notificationType,
        String title,
        String message,
        String data
    );
    
    /**
     * 사용자에게 예약된 시간에 푸시 알림을 전송합니다.
     * 
     * @param userId 사용자 식별자
     * @param notificationType 알림 타입
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param data 알림 데이터
     * @param scheduledAt 예약 시간
     */
    void sendNotification(
        UserIdentity userId,
        NotificationType notificationType,
        String title,
        String message,
        String data,
        LocalDateTime scheduledAt
    );
    
    /**
     * 대기 중인 푸시 알림들을 처리합니다.
     */
    void processPendingNotifications();
    
    /**
     * 사용자의 푸시 알림 히스토리를 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @param limit 조회 제한 수
     * @return 푸시 알림 목록
     */
    List<PushNotification> getNotificationHistory(UserIdentity userId, int limit);
    
    /**
     * 푸시 알림 통계를 조회합니다.
     * 
     * @return 알림 통계
     */
    NotificationStatistics getStatistics();
    
    /**
     * 푸시 알림을 수신 확인 처리합니다.
     * 
     * @param notificationId 알림 식별자
     */
    void markAsReceived(Long notificationId);
}