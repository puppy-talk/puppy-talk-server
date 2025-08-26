package com.puppytalk.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄러 설정 클래스
 * 
 * Spring의 스케줄링 기능을 활성화하고, 
 * NotificationScheduler의 @Scheduled 메서드들이 동작할 수 있도록 설정합니다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    
    // Spring Boot의 기본 TaskScheduler를 사용
    // 필요시 커스텀 ThreadPoolTaskScheduler 빈을 추가로 설정 가능
}
