package com.puppytalk.user.exception;

import com.puppytalk.user.UserId;

/**
 * 사용자를 찾을 수 없는 경우의 예외
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 사용자 ID로 예외 생성
     */
    public static UserNotFoundException byId(UserId userId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId.value());
    }
    
    /**
     * 사용자명으로 예외 생성
     */
    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. 사용자명: " + username);
    }
    
    /**
     * 이메일로 예외 생성
     */
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. 이메일: " + email);
    }
    
}