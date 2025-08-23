package com.puppytalk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 설정
 * 
 * Backend 관점: 안정적인 백그라운드 작업 처리
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    
    // 기본 스케줄링 설정
    // 필요에 따라 TaskScheduler Bean을 커스텀 설정 가능
}