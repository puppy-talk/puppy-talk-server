package com.puppytalk.chat.exception;

import com.puppytalk.chat.MessageId;

public class MessageNotFoundException extends RuntimeException {
    
    public MessageNotFoundException(MessageId messageId) {
        super("메시지를 찾을 수 없습니다. ID: " + messageId.getValue());
    }
}