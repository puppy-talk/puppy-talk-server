package com.puppytalk.auth;

import com.puppytalk.support.exception.DomainException;

/**
 * 인증 정보가 올바르지 않을 때 발생하는 예외
 */
public class InvalidCredentialsException extends DomainException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public String getDomainCategory() {
        return "AUTH";
    }
}