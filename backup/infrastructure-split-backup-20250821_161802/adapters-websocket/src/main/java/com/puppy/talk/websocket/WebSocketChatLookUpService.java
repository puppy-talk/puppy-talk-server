package com.puppy.talk.websocket;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.user.UserIdentity;

/**
 * WebSocket 채팅 조회 서비스 인터페이스
 */
public interface WebSocketChatLookUpService {
    
    /**
     * 채팅 메시지를 브로드캐스트합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param message 채팅 메시지
     */
    void broadcastMessage(ChatRoomIdentity chatRoomId, ChatMessage message);
    
    /**
     * 특정 사용자에게 메시지를 전송합니다.
     * 
     * @param userId 사용자 식별자
     * @param message 채팅 메시지
     */
    void sendMessageToUser(UserIdentity userId, ChatMessage message);
    
    /**
     * 사용자의 WebSocket 연결 상태를 확인합니다.
     * 
     * @param userId 사용자 식별자
     * @return 연결 상태
     */
    boolean isUserConnected(UserIdentity userId);
    
    /**
     * 채팅방의 활성 사용자 수를 조회합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @return 활성 사용자 수
     */
    int getActiveChatRoomUserCount(ChatRoomIdentity chatRoomId);
}