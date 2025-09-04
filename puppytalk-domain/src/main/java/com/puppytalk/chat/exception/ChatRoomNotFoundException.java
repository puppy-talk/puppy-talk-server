package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;

/**
 * 채팅방을 찾을 수 없을 때 발생하는 예외
 */
public class ChatRoomNotFoundException extends RuntimeException {
    
    public ChatRoomNotFoundException(String message) {
        super(message);
    }

    public ChatRoomNotFoundException(ChatRoomId chatRoomId) {
        super("채팅방을 찾을 수 없습니다. ID: " + chatRoomId.value());
    }

    public ChatRoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}