package com.puppy.talk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 인증 관련 설정
 */
@Configuration
public class AuthConfig {

    /**
     * 패스워드 해싱을 위한 BCrypt 인코더
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}