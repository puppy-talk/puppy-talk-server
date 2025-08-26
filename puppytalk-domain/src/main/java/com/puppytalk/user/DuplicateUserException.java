package com.puppytalk.user;

public class DuplicateUserException extends RuntimeException {
    
    public DuplicateUserException(String message) {
        super(message);
    }
    
    public static DuplicateUserException byUsername(String username) {
        return new DuplicateUserException("이미 존재하는 사용자명입니다: " + username);
    }
    
    public static DuplicateUserException byEmail(String email) {
        return new DuplicateUserException("이미 존재하는 이메일입니다: " + email);
    }
}