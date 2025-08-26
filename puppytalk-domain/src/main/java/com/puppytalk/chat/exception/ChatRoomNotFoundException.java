package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;

public class ChatRoomNotFoundException extends RuntimeException {
    
    public ChatRoomNotFoundException(ChatRoomId chatRoomId) {
        super("채팅방을 찾을 수 없습니다. ID: " + chatRoomId.getValue());
    }
}