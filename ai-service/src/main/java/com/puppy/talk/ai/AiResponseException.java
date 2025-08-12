package com.puppy.talk.ai;

/**
 * AI 응답 생성 중 발생하는 예외를 나타냅니다.
 */
public class AiResponseException extends RuntimeException {

    public AiResponseException(String message) {
        super(message);
    }

    public AiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
