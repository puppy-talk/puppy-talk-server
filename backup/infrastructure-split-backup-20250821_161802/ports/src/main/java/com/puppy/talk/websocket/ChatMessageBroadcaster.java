package com.puppy.talk.websocket;

import com.puppy.talk.websocket.ChatMessage;

/**
 * 채팅 메시지 브로드캐스터 인터페이스
 * 
 * 실시간 채팅 메시지 브로드캐스팅을 담당하는 서비스의 포트 인터페이스입니다.
 * 도메인 레이어에서 WebSocket 구현체에 의존하지 않고 사용할 수 있도록 합니다.
 */
public interface ChatMessageBroadcaster {
    
    /**
     * 채팅 메시지를 실시간으로 브로드캐스트합니다.
     * 
     * @param message 전송할 채팅 메시지
     * @throws RuntimeException 메시지 전송 실패 시
     */
    void broadcastMessage(ChatMessage message);
}