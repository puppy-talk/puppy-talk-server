package com.puppy.talk.activity;

import com.puppy.talk.chat.ChatRoomIdentity;

import java.time.LocalDateTime;

/**
 * 비활성 알림 도메인 모델
 */
public record InactivityNotification(
    InactivityNotificationIdentity identity,
    ChatRoomIdentity chatRoomId,
    LocalDateTime lastActivityAt,
    LocalDateTime notificationEligibleAt,
    NotificationStatus status,
    String aiGeneratedMessage,
    LocalDateTime createdAt,
    LocalDateTime sentAt
) {
    
    public InactivityNotification {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (lastActivityAt == null) {
            throw new IllegalArgumentException("LastActivityAt cannot be null");
        }
        if (notificationEligibleAt == null) {
            throw new IllegalArgumentException("NotificationEligibleAt cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }
    
    /**
     * 새로운 비활성 알림을 생성합니다.
     * 알림 대상 시간은 마지막 활동 시간 + 2시간으로 설정됩니다.
     */
    public static InactivityNotification of(
        ChatRoomIdentity chatRoomId,
        LocalDateTime lastActivityAt
    ) {
        return new InactivityNotification(
            null, // identity는 저장 시 생성됨
            chatRoomId,
            lastActivityAt,
            lastActivityAt.plusHours(2), // 2시간 후 알림 대상
            NotificationStatus.PENDING,
            null,
            LocalDateTime.now(),
            null
        );
    }
    
    /**
     * 식별자를 포함한 새로운 InactivityNotification을 생성합니다.
     */
    public InactivityNotification withIdentity(InactivityNotificationIdentity identity) {
        return new InactivityNotification(
            identity,
            this.chatRoomId,
            this.lastActivityAt,
            this.notificationEligibleAt,
            this.status,
            this.aiGeneratedMessage,
            this.createdAt,
            this.sentAt
        );
    }
    
    /**
     * 마지막 활동 시간을 업데이트합니다.
     */
    public InactivityNotification updateLastActivity(LocalDateTime newLastActivityAt) {
        return new InactivityNotification(
            this.identity,
            this.chatRoomId,
            newLastActivityAt,
            newLastActivityAt.plusHours(2), // 2시간 후로 다시 설정
            NotificationStatus.PENDING, // 상태를 다시 PENDING으로 변경
            null, // AI 생성 메시지 초기화
            this.createdAt,
            null // sentAt 초기화
        );
    }
    
    /**
     * AI가 생성한 메시지를 설정합니다.
     */
    public InactivityNotification withAiGeneratedMessage(String message) {
        return new InactivityNotification(
            this.identity,
            this.chatRoomId,
            this.lastActivityAt,
            this.notificationEligibleAt,
            this.status,
            message,
            this.createdAt,
            this.sentAt
        );
    }
    
    /**
     * 알림을 전송됨 상태로 마크합니다.
     */
    public InactivityNotification markAsSent() {
        return new InactivityNotification(
            this.identity,
            this.chatRoomId,
            this.lastActivityAt,
            this.notificationEligibleAt,
            NotificationStatus.SENT,
            this.aiGeneratedMessage,
            this.createdAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 알림을 비활성화합니다.
     */
    public InactivityNotification disable() {
        return new InactivityNotification(
            this.identity,
            this.chatRoomId,
            this.lastActivityAt,
            this.notificationEligibleAt,
            NotificationStatus.DISABLED,
            this.aiGeneratedMessage,
            this.createdAt,
            this.sentAt
        );
    }
    
    /**
     * 알림을 보낼 수 있는지 확인합니다.
     */
    public boolean isEligibleForNotification() {
        return status == NotificationStatus.PENDING && 
               LocalDateTime.now().isAfter(notificationEligibleAt);
    }
}