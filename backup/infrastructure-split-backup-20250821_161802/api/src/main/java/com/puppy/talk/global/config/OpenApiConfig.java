package com.puppy.talk.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 * API 계층에서 REST API 문서화를 담당합니다.
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final Environment environment;
    
    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI puppyTalkOpenAPI() {
        return new OpenAPI()
            .info(createApiInfo())
            .servers(createServerList())
            .components(createSecurityComponents())
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
    
    private Info createApiInfo() {
        return new Info()
            .title("Puppy Talk API")
            .description("생성형 AI 기반 반려동물 채팅 서비스 API\n\n" +
                "## 주요 기능\n" +
                "- 반려동물 생성 및 관리\n" +
                "- AI 기반 실시간 채팅\n" +
                "- 푸시 알림 서비스\n" +
                "- WebSocket 기반 실시간 통신\n\n" +
                "## 인증\n" +
                "API 사용을 위해서는 JWT 토큰이 필요합니다.\n" +
                "1. `/api/auth/login` 엔드포인트로 로그인\n" +
                "2. 반환된 JWT 토큰을 Authorization 헤더에 `Bearer {token}` 형식으로 포함")
            .version("1.0.0")
            .contact(new Contact()
                .name("Puppy Talk Team")
                .email("contact@puppytalk.com")
                .url("https://github.com/puppy-talk/puppy-talk-server"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));
    }
    
    private List<Server> createServerList() {
        if (isProductionEnvironment()) {
            return List.of(
                new Server()
                    .url("https://api.puppytalk.com")
                    .description("프로덕션 서버")
            );
        } else {
            return List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("로컬 개발 서버"),
                new Server()
                    .url("https://dev.puppytalk.com")
                    .description("개발 서버")
            );
        }
    }
    
    private Components createSecurityComponents() {
        return new Components()
            .addSecuritySchemes("Bearer Authentication", 
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT 토큰을 사용한 인증\n\n" +
                        "예: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."));
    }
    
    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("prod".equals(profile) || "production".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}