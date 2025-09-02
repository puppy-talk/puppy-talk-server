package com.puppytalk.config;

import com.puppytalk.auth.AuthenticationInterceptor;
import com.puppytalk.auth.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 설정
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final AuthenticationInterceptor authenticationInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    
    public WebMvcConfig(
        AuthenticationInterceptor authenticationInterceptor,
        CurrentUserArgumentResolver currentUserArgumentResolver
    ) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
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
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}