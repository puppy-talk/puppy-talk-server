package com.puppytalk.notification;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

public class Notification {
    private final NotificationId id;
    private final UserId userId;
    private final PetId petId;
    private final ChatRoomId chatRoomId;
    private final NotificationType type;
    private final String title;
    private final String content;
    private final NotificationStatus status;
    private final LocalDateTime scheduledAt;
    private final LocalDateTime sentAt;
    private final LocalDateTime readAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final int retryCount;
    private final String failureReason;

    private Notification(NotificationId id, UserId userId, PetId petId, ChatRoomId chatRoomId,
                        NotificationType type, String title, String content, NotificationStatus status,
                        LocalDateTime scheduledAt, LocalDateTime sentAt, LocalDateTime readAt,
                        LocalDateTime createdAt, LocalDateTime updatedAt, int retryCount, String failureReason) {
        this.id = id;
        this.userId = userId;
        this.petId = petId;
        this.chatRoomId = chatRoomId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
    }

    public static Notification createInactivityNotification(
        UserId userId,
        PetId petId,
        ChatRoomId chatRoomId,
        String title,
        String content,
        LocalDateTime scheduledAt
    ) {
        LocalDateTime now = LocalDateTime.now();
        
        return new Notification(
            null,
            userId,
            petId,
            chatRoomId,
            NotificationType.INACTIVITY_MESSAGE,
            title,
            content,
            NotificationStatus.CREATED,
            scheduledAt != null ? scheduledAt : now,
            null, // 아직 발송되지 않음
            null, // 아직 읽지 않음
            now,
            now,
            0, // 초기 재시도 횟수
            null // 실패 사유 없음
        );
    }
    

    public static Notification of(NotificationId id, UserId userId, PetId petId, ChatRoomId chatRoomId,
                                 NotificationType type, String title, String content, NotificationStatus status,
                                 LocalDateTime scheduledAt, LocalDateTime sentAt, LocalDateTime readAt,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, int retryCount, String failureReason) {

        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 알림 ID가 필요합니다");
        }

        return new Notification(id, userId, petId, chatRoomId, type, title, content, status,
                              scheduledAt, sentAt, readAt, createdAt, updatedAt, retryCount, failureReason);
    }

    /**
     * 상태 업데이트
     */
    public Notification updateStatus(NotificationStatus status) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newSentAt = this.sentAt;
        LocalDateTime newReadAt = this.readAt;
        
        if (status == NotificationStatus.SENT && this.sentAt == null) {
            newSentAt = now;
        }
        if (status == NotificationStatus.READ && this.readAt == null) {
            newReadAt = now;
        }
        
        return new Notification(
            this.id,
            this.userId,
            this.petId,
            this.chatRoomId,
            this.type,
            this.title,
            this.content,
            status,
            this.scheduledAt,
            newSentAt,
            newReadAt,
            this.createdAt,
            now, // updatedAt 갱신
            this.retryCount,
            this.failureReason
        );
    }
    
    /**
     * 재시도 증가 (실패 시)
     */
    public Notification incrementRetry(String failureReason) {
        return new Notification(
            this.id,
            this.userId,
            this.petId,
            this.chatRoomId,
            this.type,
            this.title,
            this.content,
            NotificationStatus.FAILED,
            this.scheduledAt,
            this.sentAt,
            this.readAt,
            this.createdAt,
            LocalDateTime.now(),
            this.retryCount + 1,
            failureReason
        );
    }
    
    /**
     * 발송 예정 시간인지 확인
     */
    public boolean isScheduledFor(LocalDateTime dateTime) {
        return scheduledAt.isBefore(dateTime) || scheduledAt.isEqual(dateTime);
    }
    
    /**
     * 재시도 가능한지 확인 (최대 3회)
     */
    public boolean canRetry() {
        return status.isRetryable() && retryCount < 3;
    }
    
    /**
     * 만료되었는지 확인 (24시간 기준)
     */
    public boolean isExpired() {
        LocalDateTime expiryTime = scheduledAt.plusHours(24);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    // Getters (record 스타일)
    public NotificationId id() { return id; }
    public UserId userId() { return userId; }
    public PetId petId() { return petId; }
    public ChatRoomId chatRoomId() { return chatRoomId; }
    public NotificationType type() { return type; }
    public String title() { return title; }
    public String content() { return content; }
    public NotificationStatus status() { return status; }
    public LocalDateTime scheduledAt() { return scheduledAt; }
    public LocalDateTime sentAt() { return sentAt; }
    public LocalDateTime readAt() { return readAt; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
    public int retryCount() { return retryCount; }
    public String failureReason() { return failureReason; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Notification other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type=" + type +
                ", status=" + status +
                ", title='" + title + '\'' +
                '}';
    }
}