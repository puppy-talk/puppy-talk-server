package com.puppytalk.user;

/**
 * BCrypt 기반 비밀번호 암호화 구현체
 * 
 * Spring Security Crypto의 BCryptPasswordEncoder를 위임하여 사용합니다.
 * Spring Security의 full framework 없이도 검증된 BCrypt 구현을 활용할 수 있습니다.
 */
public final class BCryptPasswordEncoder implements PasswordEncoder {
    
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder delegate;
    
    /**
     * 기본 생성자 (strength 12 사용)
     */
    public BCryptPasswordEncoder() {
        this.delegate = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12);
    }
    
    /**
     * strength를 지정하는 생성자
     * 
     * @param strength BCrypt strength (4-31, 권장: 10-12)
     */
    public BCryptPasswordEncoder(int strength) {
        this.delegate = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(strength);
    }
    
    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        
        return delegate.encode(rawPassword);
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        return delegate.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String getAlgorithm() {
        return "BCrypt";
    }
}