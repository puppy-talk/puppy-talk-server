package com.puppy.talk.event;

import java.time.LocalDateTime;

/**
 * 도메인 이벤트의 기본 인터페이스
 * 
 * 도메인 내에서 발생하는 중요한 사건들을 나타냅니다.
 * Domain Events 패턴을 통해 서비스 간 결합도를 낮춥니다.
 */
public interface DomainEvent {
    
    /**
     * 이벤트가 발생한 시간을 반환합니다.
     * 
     * @return 이벤트 발생 시간
     */
    LocalDateTime occurredOn();
    
    /**
     * 이벤트의 고유 식별자를 반환합니다.
     * 
     * @return 이벤트 ID
     */
    String eventId();
    
    /**
     * 이벤트의 타입을 반환합니다.
     * 
     * @return 이벤트 타입
     */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}