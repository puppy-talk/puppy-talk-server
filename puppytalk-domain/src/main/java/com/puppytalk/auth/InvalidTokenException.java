package com.puppytalk.auth;

import com.puppytalk.support.exception.DomainException;

/**
 * JWT 토큰이 유효하지 않을 때 발생하는 예외
 */
public class InvalidTokenException extends DomainException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static InvalidTokenException expired() {
        return new InvalidTokenException("토큰이 만료되었습니다");
    }
    
    public static InvalidTokenException invalidToken() {
        return new InvalidTokenException("유효하지 않은 토큰입니다");
    }

    @Override
    public String getDomainCategory() {
        return "AUTH";
    }
}