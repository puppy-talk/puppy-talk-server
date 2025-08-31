package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.support.exception.DomainException;

/**
 * 채팅방을 찾을 수 없을 때 발생하는 예외
 */
public class ChatRoomNotFoundException extends DomainException {
    
    public ChatRoomNotFoundException(String message) {
        super(message);
    }

    public ChatRoomNotFoundException(ChatRoomId chatRoomId) {
        super("채팅방을 찾을 수 없습니다. ID: " + chatRoomId.getValue());
    }

    public ChatRoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public String getDomainCategory() {
        return "CHAT";
    }
}