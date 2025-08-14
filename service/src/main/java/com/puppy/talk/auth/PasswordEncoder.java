package com.puppy.talk.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Spring Security 없이 BCrypt를 사용하는 패스워드 인코더
 */
@Component
public class PasswordEncoder {
    
    private static final int COST = 12;
    
    /**
     * 패스워드를 BCrypt로 해싱합니다.
     */
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }
    
    /**
     * 원본 패스워드와 해싱된 패스워드를 비교합니다.
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}