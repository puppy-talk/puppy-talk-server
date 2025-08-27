package com.puppytalk.chat;

import com.puppytalk.support.EntityId;

/**
 * 메시지 식별자
 */
public class MessageId extends EntityId {
    
    private MessageId(Long value) {
        super(value);
    }
    
    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static MessageId from(Long value) {
        return new MessageId(value);
    }
    
    /**
     * 항상 새로운 인스턴스를 생성해 반환 (신규 생성용)
     */
    public static MessageId create() {
        return new MessageId(null);
    }
}