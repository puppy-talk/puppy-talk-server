package com.puppy.talk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI puppyTalkOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Puppy Talk API")
                .description("생성형 AI 기반 반려동물 채팅 서비스 API")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Puppy Talk Team")
                    .email("contact@puppytalk.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("로컬 개발 서버"),
                new Server()
                    .url("https://api.puppytalk.com")
                    .description("프로덕션 서버")
            ));
    }
}