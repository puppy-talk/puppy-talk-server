package com.puppy.talk.ai;

/**
 * AI 응답 생성 실패 시 발생하는 예외
 */
public class AiResponseGenerationException extends RuntimeException {
    
    public AiResponseGenerationException(String message) {
        super(message);
    }
    
    public AiResponseGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AiResponseGenerationException(Throwable cause) {
        super(cause);
    }
}