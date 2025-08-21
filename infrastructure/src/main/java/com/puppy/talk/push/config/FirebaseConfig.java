package com.puppy.talk.push.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase 설정 클래스
 */
@Slf4j
@Configuration
public class FirebaseConfig {
    
    @Value("${firebase.service-account-key:firebase-service-account.json}")
    private String serviceAccountKeyPath;
    
    @Value("${firebase.project-id:puppy-talk-default}")
    private String projectId;
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Initializing Firebase App with project ID: {}", projectId);
            
            GoogleCredentials credentials = getGoogleCredentials();
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();
                
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase App initialized successfully");
            return app;
        } else {
            log.info("Firebase App already initialized, returning existing instance");
            return FirebaseApp.getInstance();
        }
    }
    
    private GoogleCredentials getGoogleCredentials() throws IOException {
        try {
            // 1. 먼저 클래스패스에서 찾기 시도
            Resource resource = new ClassPathResource(serviceAccountKeyPath);
            if (resource.exists()) {
                log.info("Loading Firebase credentials from classpath: {}", serviceAccountKeyPath);
                try (InputStream inputStream = resource.getInputStream()) {
                    return GoogleCredentials.fromStream(inputStream);
                }
            }
            
            // 2. 파일 시스템에서 찾기 시도
            log.info("Loading Firebase credentials from file system: {}", serviceAccountKeyPath);
            try (FileInputStream inputStream = new FileInputStream(serviceAccountKeyPath)) {
                return GoogleCredentials.fromStream(inputStream);
            }
            
        } catch (Exception e) {
            log.warn("Failed to load Firebase credentials from file: {}. Attempting to use default credentials.", 
                e.getMessage());
            
            // 3. 기본 애플리케이션 크리덴셜 사용 (GCP 환경)
            try {
                return GoogleCredentials.getApplicationDefault();
            } catch (Exception defaultException) {
                log.warn("Failed to load default credentials: {}. Using mock credentials for development.", 
                    defaultException.getMessage());
                
                // 4. 개발 환경용 Mock 크리덴셜 (실제 푸시는 전송되지 않음)
                return createMockCredentials();
            }
        }
    }
    
    private GoogleCredentials createMockCredentials() {
        log.warn("Using mock Firebase credentials - push notifications will not be sent!");
        // 개발 환경에서 Firebase 초기화를 위한 임시 크리덴셜
        // 실제로는 푸시 알림이 전송되지 않습니다
        return GoogleCredentials.newBuilder().build();
    }
}