package com.puppytalk.support.exception;

public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 예외의 도메인 카테고리를 반환한다.
     * 로깅 및 모니터링에서 분류 목적으로 사용된다.
     */
    public abstract String getDomainCategory();

    /**
     * 사용자에게 표시될 수 있는 메시지인지 여부를 반환한다.
     */
    public boolean isUserFriendly() {
        return true;
    }
}