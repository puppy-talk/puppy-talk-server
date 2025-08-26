package com.puppytalk.chat.exception;

public class MessageValidationException extends RuntimeException {
    
    public MessageValidationException(String message) {
        super(message);
    }
    
    public static MessageValidationException contentEmpty() {
        return new MessageValidationException("메시지 내용이 비어있습니다");
    }
    
    public static MessageValidationException contentTooLong(int maxLength) {
        return new MessageValidationException("메시지 길이가 너무 깁니다. 최대 " + maxLength + "자까지 가능합니다");
    }
}