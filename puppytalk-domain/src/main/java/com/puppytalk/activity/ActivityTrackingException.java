package com.puppytalk.activity;

import com.puppytalk.user.UserId;

/**
 * 활동 추적 관련 예외
 * 
 * Backend 관점: 시스템 안정성을 위한 명확한 예외 처리
 */
public class ActivityTrackingException extends RuntimeException {
    
    private final UserId userId;
    private final ActivityType activityType;
    
    public ActivityTrackingException(String message) {
        super(message);
        this.userId = null;
        this.activityType = null;
    }
    
    public ActivityTrackingException(String message, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.activityType = null;
    }
    
    public ActivityTrackingException(String message, UserId userId, ActivityType activityType) {
        super(message);
        this.userId = userId;
        this.activityType = activityType;
    }
    
    public ActivityTrackingException(String message, UserId userId, ActivityType activityType, Throwable cause) {
        super(message, cause);
        this.userId = userId;
        this.activityType = activityType;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    /**
     * 활동 추적 실패 예외 생성
     */
    public static ActivityTrackingException trackingFailed(UserId userId, ActivityType activityType, String reason) {
        return new ActivityTrackingException(
            String.format("Activity tracking failed for user %s with activity %s: %s", 
                userId != null ? userId.getValue() : "unknown", 
                activityType != null ? activityType.name() : "unknown", 
                reason),
            userId, 
            activityType
        );
    }
    
    /**
     * 활동 데이터 손상 예외 생성
     */
    public static ActivityTrackingException dataCorrupted(String details) {
        return new ActivityTrackingException("Activity data corrupted: " + details);
    }
    
    /**
     * 비활성 사용자 감지 실패 예외 생성
     */
    public static ActivityTrackingException inactiveUserDetectionFailed(String reason) {
        return new ActivityTrackingException("Inactive user detection failed: " + reason);
    }
}