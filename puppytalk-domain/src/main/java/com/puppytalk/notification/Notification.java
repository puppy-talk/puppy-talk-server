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

    private Notification(NotificationId id, UserId userId, PetId petId, ChatRoomId chatRoomId,
                        NotificationType type, String title, String content, NotificationStatus status,
                        LocalDateTime scheduledAt, LocalDateTime sentAt, LocalDateTime readAt,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
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
            now
        );
    }
    

    public static Notification of(NotificationId id, UserId userId, PetId petId, ChatRoomId chatRoomId,
                                 NotificationType type, String title, String content, NotificationStatus status,
                                 LocalDateTime scheduledAt, LocalDateTime sentAt, LocalDateTime readAt,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {

        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 알림 ID가 필요합니다");
        }

        return new Notification(id, userId, petId, chatRoomId, type, title, content, status,
                              scheduledAt, sentAt, readAt, createdAt, updatedAt);
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
            now
        );
    }

    public NotificationId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public PetId getPetId() {
        return petId;
    }

    public ChatRoomId getChatRoomId() {
        return chatRoomId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification that = (Notification) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUserId(),
            that.getUserId()) && Objects.equals(getPetId(), that.getPetId())
            && Objects.equals(getChatRoomId(), that.getChatRoomId())
            && getType() == that.getType() && Objects.equals(getTitle(), that.getTitle())
            && Objects.equals(getContent(), that.getContent())
            && getStatus() == that.getStatus() && Objects.equals(getScheduledAt(),
            that.getScheduledAt()) && Objects.equals(getSentAt(), that.getSentAt())
            && Objects.equals(getReadAt(), that.getReadAt()) && Objects.equals(
            getCreatedAt(), that.getCreatedAt()) && Objects.equals(getUpdatedAt(),
            that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserId(), getPetId(), getChatRoomId(), getType(),
            getTitle(),
            getContent(), getStatus(), getScheduledAt(), getSentAt(), getReadAt(), getCreatedAt(),
            getUpdatedAt());
    }
}