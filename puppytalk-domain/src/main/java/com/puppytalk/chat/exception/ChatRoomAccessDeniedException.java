package com.puppytalk.chat.exception;

import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.user.UserId;

/**
 * 채팅방 접근 권한이 없을 때 발생하는 예외
 */
public class ChatRoomAccessDeniedException extends RuntimeException {
    
    private final UserId userId;
    private final ChatRoomId chatRoomId;
    
    public ChatRoomAccessDeniedException(String message, UserId userId, ChatRoomId chatRoomId) {
        super(message);
        this.userId = userId;
        this.chatRoomId = chatRoomId;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public ChatRoomId getChatRoomId() {
        return chatRoomId;
    }
    
    @Override
    public String toString() {
        return String.format("ChatRoomAccessDeniedException{message='%s', userId=%s, chatRoomId=%s}", 
            getMessage(), userId, chatRoomId);
    }
}