package com.puppy.talk.push;

import com.puppy.talk.user.UserIdentity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 푸시 알림 저장소 인터페이스
 */
public interface PushNotificationRepository {
    
    /**
     * 푸시 알림을 저장합니다.
     */
    PushNotification save(PushNotification notification);
    
    /**
     * 식별자로 푸시 알림을 조회합니다.
     */
    Optional<PushNotification> findByIdentity(PushNotificationIdentity identity);
    
    /**
     * 사용자의 푸시 알림 목록을 조회합니다.
     */
    List<PushNotification> findByUserId(UserIdentity userId);
    
    /**
     * 전송 대기 중인 푸시 알림들을 조회합니다.
     */
    List<PushNotification> findPendingNotifications();
    
    /**
     * 특정 시간 이전에 생성된 전송 대기 중인 푸시 알림들을 조회합니다.
     */
    List<PushNotification> findPendingNotificationsBefore(LocalDateTime before);
    
    /**
     * 상태별 푸시 알림 개수를 조회합니다.
     */
    long countByStatus(PushNotificationStatus status);
    
    /**
     * 전체 푸시 알림 개수를 조회합니다.
     */
    long count();
    
    /**
     * 사용자의 최근 푸시 알림들을 조회합니다.
     */
    List<PushNotification> findRecentByUserId(UserIdentity userId, int limit);
}