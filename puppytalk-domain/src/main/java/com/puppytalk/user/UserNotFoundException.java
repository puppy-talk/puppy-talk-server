package com.puppytalk.user;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public static UserNotFoundException byId(UserId userId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId.getValue());
    }
    
    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. 사용자명: " + username);
    }
    
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. 이메일: " + email);
    }
}