package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.support.exception.DomainException;

/**
 * 채팅방을 찾을 수 없는 경우의 예외
 */
public class ChatRoomNotFoundException extends DomainException {
    
    public ChatRoomNotFoundException(ChatRoomId chatRoomId) {
        super("채팅방을 찾을 수 없습니다. ID: " + chatRoomId.getValue());
    }
    
    @Override
    public String getDomainCategory() {
        return "CHATROOM_NOT_FOUND";
    }
}