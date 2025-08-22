package com.puppytalk.user;

/**
 * 중복된 사용자가 존재할 때 발생하는 예외
 */
public class DuplicateUserException extends RuntimeException {
    
    public DuplicateUserException(String username) {
        super("이미 존재하는 사용자명입니다: " + username);
    }
    
    public static DuplicateUserException byEmail(String email) {
        return new DuplicateUserException("이미 존재하는 이메일입니다: " + email);
    }
}