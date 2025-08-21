package com.puppy.talk.push.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 푸시 서비스 설정 클래스
 */
@Slf4j
@Configuration
public class PushServiceConfig {
    
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        log.info("Creating FirebaseMessaging bean");
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}