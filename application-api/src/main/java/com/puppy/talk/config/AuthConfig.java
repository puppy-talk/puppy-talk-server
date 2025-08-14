package com.puppy.talk.config;

import org.springframework.context.annotation.Configuration;

/**
 * 인증 관련 설정
 * Spring Security 없이 직접 구현
 */
@Configuration
public class AuthConfig {
    // Spring Security 사용 금지 정책에 따라 Bean 설정 제거
    // PasswordEncoder는 service 모듈에서 직접 구현
}