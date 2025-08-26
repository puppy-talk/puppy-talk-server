package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

public class ChatRoomAccessDeniedException extends RuntimeException {
    
    public ChatRoomAccessDeniedException(String message, UserId userId, ChatRoomId chatRoomId) {
        super(message + " (User: " + userId.getValue() + ", ChatRoom: " + chatRoomId.getValue() + ")");
    }
}