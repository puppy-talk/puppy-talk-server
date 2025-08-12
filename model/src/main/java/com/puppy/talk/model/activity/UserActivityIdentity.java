package com.puppy.talk.model.activity;

/**
 * 사용자 활동 식별자
 */
public record UserActivityIdentity(Long id) {
    
    public UserActivityIdentity {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("UserActivity identity must be positive");
        }
    }
    
    public static UserActivityIdentity of(Long id) {
        return new UserActivityIdentity(id);
    }
}