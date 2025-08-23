package com.puppytalk.chat.exception;

import com.puppytalk.chat.MessageId;

/**
 * 메시지를 찾을 수 없을 때 발생하는 예외
 */
public class MessageNotFoundException extends RuntimeException {
    
    public MessageNotFoundException(MessageId messageId) {
        super("메시지를 찾을 수 없습니다. ID: " + messageId);
    }
    
    public MessageNotFoundException(String message) {
        super(message);
    }
}