package com.puppy.talk.notification;

import com.puppy.talk.websocket.ChatMessage;
import com.puppy.talk.user.UserIdentity;

/**
 * 실시간 알림 전송을 위한 포트 인터페이스
 * Hexagonal Architecture에서 Service 레이어가 실시간 알림 기능에 접근하기 위한 추상화
 */
public interface RealtimeNotificationPort {
    
    /**
     * 특정 채팅방의 모든 참여자에게 메시지를 실시간 브로드캐스트합니다.
     *
     * @param message 브로드캐스트할 채팅 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    void broadcastMessage(ChatMessage message);
    
    /**
     * 특정 채팅방의 모든 참여자에게 타이핑 상태를 실시간 브로드캐스트합니다.
     *
     * @param typingMessage 타이핑 상태 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    void broadcastTypingStatus(ChatMessage typingMessage);
    
    /**
     * 특정 채팅방의 모든 참여자에게 읽음 확인을 실시간 브로드캐스트합니다.
     *
     * @param readReceiptMessage 읽음 확인 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    void broadcastReadReceipt(ChatMessage readReceiptMessage);
    
    /**
     * 특정 채팅방에 시스템 메시지를 실시간 브로드캐스트합니다.
     *
     * @param systemMessage 시스템 메시지
     * @throws RealtimeNotificationException 브로드캐스트 실패 시
     */
    void broadcastSystemMessage(ChatMessage systemMessage);
    
    /**
     * 특정 사용자에게 개인 메시지를 실시간 전송합니다.
     *
     * @param user 전송할 사용자 식별자
     * @param message 전송할 채팅 메시지
     * @throws RealtimeNotificationException 개인 전송 실패 시
     */
    void sendToUser(UserIdentity user, ChatMessage message);
}
