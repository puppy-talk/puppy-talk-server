package com.puppytalk.config;

import com.puppytalk.auth.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final AuthenticationInterceptor authenticationInterceptor;
    
    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**")  // 모든 API 요청에 인터셉터 적용
            .excludePathPatterns(
                "/api/auth/login",           // 로그인 API 제외
                "/api/users",                // 회원가입 API 제외 (POST /api/users)
                "/actuator/**",              // Actuator 엔드포인트 제외
                "/h2-console/**",            // H2 Console 제외
                "/swagger-ui/**",            // Swagger UI 제외
                "/v3/api-docs/**"            // OpenAPI 문서 제외
            );
    }
}