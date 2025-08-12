package com.puppy.talk.exception.activity;

/**
 * 활동 추적 중 발생하는 예외
 */
public class ActivityTrackingException extends RuntimeException {
    
    public ActivityTrackingException(String message) {
        super(message);
    }
    
    public ActivityTrackingException(String message, Throwable cause) {
        super(message, cause);
    }
}