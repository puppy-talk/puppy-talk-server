package com.puppytalk.notification;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 알림 도메인 모델
 * 
 * Backend 관점: 메시지 전달 보장과 추적 가능성 중심의 설계
 */
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
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("NotificationType must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("NotificationStatus must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be null or empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt must not be null");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("RetryCount must not be negative");
        }

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
    
    /**
     * 비활성 사용자 알림 생성 (정적 팩토리 메서드)
     */
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
            null, // ID는 저장 시 생성
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
    
    /**
     * 시스템 알림 생성
     */
    public static Notification createSystemNotification(
        UserId userId,
        String title,
        String content
    ) {
        LocalDateTime now = LocalDateTime.now();
        
        return new Notification(
            null,
            userId,
            null, // 시스템 알림은 반려동물 없음
            null, // 시스템 알림은 채팅방 없음
            NotificationType.SYSTEM_NOTIFICATION,
            title,
            content,
            NotificationStatus.CREATED,
            now, // 즉시 발송
            null,
            null,
            now,
            now,
            0,
            null
        );
    }

    /**
     * 기존 알림 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static Notification of(NotificationId id, UserId userId, PetId petId, ChatRoomId chatRoomId,
                                 NotificationType type, String title, String content, NotificationStatus status,
                                 LocalDateTime scheduledAt, LocalDateTime sentAt, LocalDateTime readAt,
                                 LocalDateTime createdAt, LocalDateTime updatedAt, int retryCount, String failureReason) {
        if (id == null || !id.isValid()) {
            throw new IllegalArgumentException("저장된 알림 ID가 필요합니다");
        }

        return new Notification(id, userId, petId, chatRoomId, type, title, content, status,
                              scheduledAt, sentAt, readAt, createdAt, updatedAt, retryCount, failureReason);
    }
    
    /**
     * ID를 포함한 새로운 Notification 생성 (Repository에서 사용)
     */
    public Notification withId(NotificationId id) {
        return new Notification(
            id,
            this.userId,
            this.petId,
            this.chatRoomId,
            this.type,
            this.title,
            this.content,
            this.status,
            this.scheduledAt,
            this.sentAt,
            this.readAt,
            this.createdAt,
            this.updatedAt,
            this.retryCount,
            this.failureReason
        );
    }
    
    /**
     * 상태 업데이트 (불변 객체 패턴)
     */
    public Notification updateStatus(NotificationStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newSentAt = this.sentAt;
        LocalDateTime newReadAt = this.readAt;
        
        // 상태별 타임스탬프 업데이트
        if (newStatus == NotificationStatus.SENT && this.sentAt == null) {
            newSentAt = now;
        }
        if (newStatus == NotificationStatus.READ && this.readAt == null) {
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
            newStatus,
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

    // getter
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