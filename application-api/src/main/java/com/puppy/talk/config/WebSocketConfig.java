package com.puppy.talk.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 및 STOMP 설정
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final Environment environment;
    
    @Value("${puppy-talk.websocket.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지를 보낼 때 사용하는 prefix
        config.enableSimpleBroker("/topic", "/queue");
        
        // 클라이언트에서 서버로 메시지를 보낼 때 사용하는 prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // 특정 사용자에게 메시지를 보낼 때 사용하는 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        var endpoint = registry.addEndpoint("/ws");
        
        // 개발 환경에서는 모든 Origin 허용, 프로덕션에서는 설정된 Origin만 허용
        if (isDevEnvironment()) {
            endpoint.setAllowedOriginPatterns("*"); // 개발 환경에서만 모든 Origin 허용
        } else {
            endpoint.setAllowedOrigins(allowedOrigins); // 설정에서 허용된 Origin만 허용
        }
        
        endpoint.withSockJS(); // SockJS fallback 지원
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 인바운드 메시지에 인터셉터 추가
        registration.interceptors(webSocketAuthInterceptor);
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 아웃바운드 메시지 순서 보존을 위해 순차 실행 Executor 설정
        // Spring Boot 3.x에서는 setPreservePublishOrder 대신 단일 스레드 Executor로 메시지 순서 보장
        registration.taskExecutor()
            .corePoolSize(1)        // 핵심 스레드 풀 크기 1 (단일 스레드)
            .maxPoolSize(1)         // 최대 스레드 풀 크기 1 (단일 스레드)
            .queueCapacity(1000);   // 대기 큐 용량 (메시지 버퍼링)
    }
    
    private boolean isDevEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("local".equals(profile) || "dev".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}