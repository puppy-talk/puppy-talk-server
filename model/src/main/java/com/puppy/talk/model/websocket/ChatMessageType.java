package com.puppy.talk.model.websocket;

/**
 * WebSocket 채팅 메시지 타입
 */
public enum ChatMessageType {
    /**
     * 일반 채팅 메시지
     */
    MESSAGE,
    
    /**
     * 타이핑 시작
     */
    TYPING,
    
    /**
     * 타이핑 중단
     */
    STOP_TYPING,
    
    /**
     * 메시지 읽음 확인
     */
    READ_RECEIPT,
    
    /**
     * 사용자 입장
     */
    USER_JOINED,
    
    /**
     * 사용자 퇴장
     */
    USER_LEFT,
    
    /**
     * 시스템 메시지
     */
    SYSTEM
}