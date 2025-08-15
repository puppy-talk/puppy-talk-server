package com.puppy.talk.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationEventPublisher를 사용한 도메인 이벤트 발행 구현체
 * 
 * DomainEventPublisher 인터페이스의 구현체로,
 * Spring의 이벤트 메커니즘을 활용하여 도메인 이벤트를 발행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null domain event");
            return;
        }

        try {
            log.debug("Publishing domain event: {} with ID: {}", 
                event.eventType(), event.eventId());
                
            applicationEventPublisher.publishEvent(event);
            
            log.debug("Successfully published domain event: {}", event.eventType());
            
        } catch (Exception e) {
            log.error("Failed to publish domain event: {} with ID: {}", 
                event.eventType(), event.eventId(), e);
            
            // 이벤트 발행 실패가 비즈니스 로직을 중단시키지 않도록 예외를 잡습니다.
            // 필요에 따라 실패한 이벤트를 별도 저장소에 기록하거나 재시도 로직을 추가할 수 있습니다.
        }
    }
}