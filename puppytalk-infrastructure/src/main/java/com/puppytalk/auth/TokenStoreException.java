package com.puppytalk.auth;

/**
 * 토큰 저장소 관련 예외
 */
public class TokenStoreException extends RuntimeException {
    
    public TokenStoreException(String message) {
        super(message);
    }
    
    public TokenStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}