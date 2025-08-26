package com.puppytalk.activity;

public class ActivityTrackingException extends RuntimeException {
    
    public ActivityTrackingException(String message) {
        super(message);
    }
    
    public ActivityTrackingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ActivityTrackingException trackingFailed(String reason) {
        return new ActivityTrackingException("활동 추적에 실패했습니다: " + reason);
    }
    
    public static ActivityTrackingException inactiveUserDetectionFailed(String reason) {
        return new ActivityTrackingException("비활성 사용자 탐지에 실패했습니다: " + reason);
    }
}