package com.puppy.talk.event;

/**
 * 도메인 이벤트 발행을 위한 포트 인터페이스
 * 
 * Domain Events 패턴에서 이벤트 발행을 담당합니다.
 * Business Logic Layer에서 이 인터페이스를 통해 이벤트를 발행하고,
 * Infrastructure Layer에서 구체적인 구현을 제공합니다.
 */
public interface DomainEventPublisher {
    
    /**
     * 도메인 이벤트를 발행합니다.
     * 
     * @param event 발행할 도메인 이벤트
     */
    void publish(DomainEvent event);
    
    /**
     * 여러 도메인 이벤트를 배치로 발행합니다.
     * 
     * @param events 발행할 도메인 이벤트들
     */
    default void publishAll(DomainEvent... events) {
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}