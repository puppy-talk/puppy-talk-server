package com.puppytalk.auth;

import com.puppytalk.user.User;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) 
               && User.class.isAssignableFrom(parameter.getParameterType());
    }
    
    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        
        if (request == null) {
            throw new IllegalStateException("HttpServletRequest를 찾을 수 없습니다");
        }
        
        User currentUser = (User) request.getAttribute(AuthenticationInterceptor.CURRENT_USER_ATTRIBUTE);
        
        if (currentUser == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다");
        }
        
        return currentUser;
    }
}