package com.puppytalk.notification;

import com.puppytalk.infrastructure.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 알림 JPA 엔티티
 * 
 * Backend 최적화: 대용량 알림 처리를 위한 인덱스 전략
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_status_scheduled", columnList = "status, scheduled_at"),
    @Index(name = "idx_notifications_user_status", columnList = "user_id, status"),
    @Index(name = "idx_notifications_type_status", columnList = "type, status"),
    @Index(name = "idx_notifications_scheduled_at", columnList = "scheduled_at"),
    @Index(name = "idx_notifications_retry_status", columnList = "retry_count, status")
})
public class NotificationJpaEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "pet_id")
    private Long petId; // null 가능 (시스템 알림 시)
    
    @Column(name = "chat_room_id")
    private Long chatRoomId; // null 가능 (시스템 알림 시)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;
    
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    protected NotificationJpaEntity() {
        // JPA 기본 생성자
    }
    
    private NotificationJpaEntity(
        Long id, Long userId, Long petId, Long chatRoomId,
        NotificationType type, String title, String content,
        NotificationStatus status, LocalDateTime scheduledAt,
        LocalDateTime sentAt, LocalDateTime readAt,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        int retryCount, String failureReason
    ) {
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
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        // BaseEntity의 타임스탬프는 JPA 어노테이션으로 관리됨
        if (createdAt != null) this.createdAt = createdAt;
        if (updatedAt != null) this.updatedAt = updatedAt;
    }
    
    public static NotificationJpaEntity fromDomain(Notification notification) {
        return new NotificationJpaEntity(
            notification.id() != null ? notification.id().getValue() : null,
            notification.userId().getValue(),
            notification.petId() != null ? notification.petId().getValue() : null,
            notification.chatRoomId() != null ? notification.chatRoomId().getValue() : null,
            notification.type(),
            notification.title(),
            notification.content(),
            notification.status(),
            notification.scheduledAt(),
            notification.sentAt(),
            notification.readAt(),
            notification.createdAt(),
            notification.updatedAt(),
            notification.retryCount(),
            notification.failureReason()
        );
    }
    
    public Notification toDomain() {
        return new Notification(
            id != null ? NotificationId.of(id) : null,
            com.puppytalk.user.UserId.of(userId),
            petId != null ? com.puppytalk.pet.PetId.of(petId) : null,
            chatRoomId != null ? com.puppytalk.chat.ChatRoomId.of(chatRoomId) : null,
            type,
            title,
            content,
            status,
            scheduledAt,
            sentAt,
            readAt,
            getCreatedAt(),
            getUpdatedAt(),
            retryCount,
            failureReason
        );
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getPetId() { return petId; }
    public Long getChatRoomId() { return chatRoomId; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public NotificationStatus getStatus() { return status; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public int getRetryCount() { return retryCount; }
    public String getFailureReason() { return failureReason; }
    
    // Status update methods for JPA
    public void updateStatus(NotificationStatus newStatus) {
        this.status = newStatus;
        if (newStatus == NotificationStatus.SENT && this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        if (newStatus == NotificationStatus.READ && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementRetry(String reason) {
        this.retryCount++;
        this.status = NotificationStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
}