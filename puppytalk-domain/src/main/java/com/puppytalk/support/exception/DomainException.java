package com.puppytalk.support.exception;

public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 도메인 예외의 카테고리를 반환합니다.
     */
    public abstract String getDomainCategory();
}