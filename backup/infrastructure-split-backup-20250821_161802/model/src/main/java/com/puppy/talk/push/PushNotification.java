package com.puppy.talk.push;

import com.puppy.talk.user.UserIdentity;
import java.time.LocalDateTime;

/**
 * 푸시 알림 도메인 모델
 */
public record PushNotification(
    PushNotificationIdentity identity,
    UserIdentity userId,
    String deviceToken,
    NotificationType notificationType,
    String title,
    String message,
    String data, // JSON 형태의 추가 데이터
    PushNotificationStatus status,
    String errorMessage,
    LocalDateTime scheduledAt,
    LocalDateTime sentAt,
    LocalDateTime createdAt
) {

    public PushNotification {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            throw new IllegalArgumentException("DeviceToken cannot be null or empty");
        }
        if (notificationType == null) {
            throw new IllegalArgumentException("NotificationType cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }

        // normalize strings
        deviceToken = deviceToken.trim();
        title = title.trim();
        message = message.trim();
        if (data != null) {
            data = data.trim();
        }
        if (errorMessage != null) {
            errorMessage = errorMessage.trim();
        }
    }

    /**
     * 새로운 푸시 알림을 생성합니다.
     */
    public static PushNotification of(
        UserIdentity userId,
        String deviceToken,
        NotificationType notificationType,
        String title,
        String message,
        String data,
        LocalDateTime scheduledAt
    ) {
        return new PushNotification(
            null, // identity는 저장 시 생성됨
            userId,
            deviceToken,
            notificationType,
            title,
            message,
            data,
            PushNotificationStatus.PENDING,
            null,
            scheduledAt,
            null,
            LocalDateTime.now()
        );
    }

    /**
     * 즉시 전송할 푸시 알림을 생성합니다.
     */
    public static PushNotification of(
        UserIdentity userId,
        String deviceToken,
        NotificationType notificationType,
        String title,
        String message,
        String data
    ) {
        return of(userId, deviceToken, notificationType, title, message, data, LocalDateTime.now());
    }

    /**
     * 식별자를 포함한 새로운 PushNotification을 생성합니다.
     */
    public PushNotification withIdentity(PushNotificationIdentity identity) {
        return new PushNotification(
            identity,
            this.userId,
            this.deviceToken,
            this.notificationType,
            this.title,
            this.message,
            this.data,
            this.status,
            this.errorMessage,
            this.scheduledAt,
            this.sentAt,
            this.createdAt
        );
    }

    /**
     * 전송 완료로 상태를 변경합니다.
     */
    public PushNotification markAsSent() {
        return new PushNotification(
            this.identity,
            this.userId,
            this.deviceToken,
            this.notificationType,
            this.title,
            this.message,
            this.data,
            PushNotificationStatus.SENT,
            null,
            this.scheduledAt,
            LocalDateTime.now(),
            this.createdAt
        );
    }

    /**
     * 전송 실패로 상태를 변경합니다.
     */
    public PushNotification markAsFailed(String errorMessage) {
        return new PushNotification(
            this.identity,
            this.userId,
            this.deviceToken,
            this.notificationType,
            this.title,
            this.message,
            this.data,
            PushNotificationStatus.FAILED,
            errorMessage,
            this.scheduledAt,
            LocalDateTime.now(),
            this.createdAt
        );
    }

    /**
     * 수신 확인으로 상태를 변경합니다.
     */
    public PushNotification markAsReceived() {
        return new PushNotification(
            this.identity,
            this.userId,
            this.deviceToken,
            this.notificationType,
            this.title,
            this.message,
            this.data,
            PushNotificationStatus.RECEIVED,
            this.errorMessage,
            this.scheduledAt,
            this.sentAt,
            this.createdAt
        );
    }

    /**
     * 푸시 알림을 전송할 시간이 되었는지 확인합니다.
     */
    public boolean isReadyToSend() {
        return status == PushNotificationStatus.PENDING &&
            LocalDateTime.now().isAfter(scheduledAt);
    }
}