package com.puppy.talk.infrastructure.activity;

import com.puppy.talk.model.activity.InactivityNotification;
import com.puppy.talk.model.activity.InactivityNotificationIdentity;
import com.puppy.talk.model.activity.NotificationStatus;
import com.puppy.talk.model.chat.ChatRoomIdentity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 비활성 알림 저장소 인터페이스
 */
public interface InactivityNotificationRepository {
    
    /**
     * 비활성 알림을 저장합니다.
     */
    InactivityNotification save(InactivityNotification notification);
    
    /**
     * 비활성 알림을 식별자로 조회합니다.
     */
    Optional<InactivityNotification> findByIdentity(InactivityNotificationIdentity identity);
    
    /**
     * 채팅방별 비활성 알림을 조회합니다.
     */
    Optional<InactivityNotification> findByChatRoomId(ChatRoomIdentity chatRoomId);
    
    /**
     * 특정 상태의 비활성 알림들을 조회합니다.
     */
    List<InactivityNotification> findByStatus(NotificationStatus status);
    
    /**
     * 알림 대상 시간이 지난 PENDING 상태의 알림들을 조회합니다.
     */
    List<InactivityNotification> findEligibleNotifications();
    
    /**
     * 특정 시간 이전에 생성된 알림들을 조회합니다.
     */
    List<InactivityNotification> findByCreatedAtBefore(LocalDateTime beforeTime);
    
    /**
     * 채팅방의 비활성 알림을 삭제합니다.
     */
    void deleteByChatRoomId(ChatRoomIdentity chatRoomId);
    
    /**
     * 비활성 알림을 삭제합니다.
     */
    void delete(InactivityNotification notification);
    
    /**
     * 모든 비활성 알림의 개수를 조회합니다.
     */
    long count();
    
    /**
     * 상태별 비활성 알림의 개수를 조회합니다.
     */
    long countByStatus(NotificationStatus status);
}