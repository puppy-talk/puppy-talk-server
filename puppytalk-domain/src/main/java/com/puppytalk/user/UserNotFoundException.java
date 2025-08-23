package com.puppytalk.user;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(UserId userId) {
        super("사용자를 찾을 수 없습니다. ID: " + userId.value());
    }
    
    public UserNotFoundException(String identifier) {
        super("사용자를 찾을 수 없습니다. 식별자: " + identifier);
    }
    
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("이메일로 사용자를 찾을 수 없습니다: " + email);
    }
}