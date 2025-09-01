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
    
    public static InvalidTokenException invalid() {
        return new InvalidTokenException("유효하지 않은 토큰입니다");
    }
    
    public static InvalidTokenException malformed() {
        return new InvalidTokenException("토큰 형식이 올바르지 않습니다");
    }
    
    public static InvalidTokenException unsupported() {
        return new InvalidTokenException("지원하지 않는 토큰입니다");
    }
    
    @Override
    public String getDomainCategory() {
        return "AUTH";
    }
}