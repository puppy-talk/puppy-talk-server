package com.puppy.talk.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.user.UserIdentity;

/**
 * WebSocket 관련 조회 서비스 인터페이스
 */
public interface WebSocketLookUpService {
    
    /**
     * 사용자 메시지를 처리합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param userId 사용자 식별자
     * @param content 메시지 내용
     */
    void processUserMessage(ChatRoomIdentity chatRoomId, UserIdentity userId, String content);
    
    /**
     * 타이핑 상태를 처리합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param userId 사용자 식별자
     * @param isTyping 타이핑 여부
     */
    void processTypingStatus(ChatRoomIdentity chatRoomId, UserIdentity userId, boolean isTyping);
    
    /**
     * 메시지 읽음 상태를 처리합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param userId 사용자 식별자
     */
    void processReadReceipt(ChatRoomIdentity chatRoomId, UserIdentity userId);
}