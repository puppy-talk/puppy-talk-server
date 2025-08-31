package com.puppytalk.activity;

import com.puppytalk.infrastructure.common.BaseEntity;
import com.puppytalk.user.UserId;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 사용자 활동 JPA 엔티티
 * 
 * Backend 최적화: 성능 중심의 인덱스와 쿼리 설계
 */
@Entity
@Table(name = "user_activities", indexes = {
    @Index(name = "idx_user_activities_user_id", columnList = "user_id"),
    @Index(name = "idx_user_activities_user_activity_desc", columnList = "user_id, activity_at DESC"),
    @Index(name = "idx_user_activities_activity_at_desc", columnList = "activity_at DESC"),
    @Index(name = "idx_user_activities_type_activity_at", columnList = "activity_type, activity_at DESC")
})
public class UserActivityJpaEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "chat_room_id")
    private Long chatRoomId; // null 가능 (LOGIN 시)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 20)
    private ActivityType activityType;
    
    @Column(name = "activity_at", nullable = false)
    private LocalDateTime activityAt;
    
    protected UserActivityJpaEntity() {
        // JPA 기본 생성자
    }
    
    private UserActivityJpaEntity(Long id, Long userId, Long chatRoomId, 
                                 ActivityType activityType, LocalDateTime activityAt,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.chatRoomId = chatRoomId;
        this.activityType = activityType;
        this.activityAt = activityAt;
    }
    
    /**
     * model -> jpa entity
     */
    public static UserActivityJpaEntity from(UserActivity activity) {
        return new UserActivityJpaEntity(
            activity.id() != null ? activity.id().getValue() : null,
            activity.userId().getValue(),
            activity.chatRoomId() != null ? activity.chatRoomId().getValue() : null,
            activity.activityType(),
            activity.activityAt(),
            activity.createdAt()
        );
    }
    
    /**
     * jpa entity -> model
     */
    public UserActivity toDomain() {
        if (id != null) {
            return UserActivity.of(
                ActivityId.from(id),
                UserId.from(userId),
                chatRoomId != null ? com.puppytalk.chat.ChatRoomId.from(chatRoomId) : null,
                activityType,
                activityAt,
                getCreatedAt()
            );
        } else {
            // 새로운 활동 생성 (ID가 없는 경우)
            if (chatRoomId != null) {
                return UserActivity.createActivity(
                    UserId.from(userId),
                    com.puppytalk.chat.ChatRoomId.from(chatRoomId),
                    activityType,
                    activityAt
                );
            } else {
                return UserActivity.createGlobalActivity(
                    UserId.from(userId),
                    activityType,
                    activityAt
                );
            }
        }
    }
    
    // getter
    public Long getId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Long getChatRoomId() {
        return chatRoomId;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public LocalDateTime getActivityAt() {
        return activityAt;
    }
}