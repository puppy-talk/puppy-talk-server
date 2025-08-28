package com.puppytalk.user;

import com.puppytalk.support.exception.DomainException;

/**
 * 사용자를 찾을 수 없는 경우의 예외
 */
public class UserNotFoundException extends DomainException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    /**
     * 사용자 ID로 예외 생성
     */
    public static UserNotFoundException byId(UserId userId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId.getValue());
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
    
    @Override
    public String getDomainCategory() {
        return "USER_NOT_FOUND";
    }
}