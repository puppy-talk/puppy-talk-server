package com.puppy.talk.auth;

import com.google.common.hash.Hashing;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Guava를 사용한 패스워드 해싱 처리
 */
@Component
public class PasswordEncoder {
    
    private static final int SALT_LENGTH = 16;
    private static final String SEPARATOR = "$";
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 패스워드를 Guava Hashing과 Salt를 사용하여 해싱합니다.
     */
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        
        // 랜덤 salt 생성
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        
        // 패스워드와 salt를 결합하여 해싱
        String saltedPassword = rawPassword + Base64.getEncoder().encodeToString(salt);
        String hashedPassword = Hashing.sha256()
            .hashString(saltedPassword, StandardCharsets.UTF_8)
            .toString();
        
        // salt와 해시된 패스워드를 함께 저장 (format: salt$hashedPassword)
        return Base64.getEncoder().encodeToString(salt) + SEPARATOR + hashedPassword;
    }
    
    /**
     * 원본 패스워드와 해싱된 패스워드를 비교합니다.
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            // 저장된 패스워드에서 salt와 해시 분리
            String[] parts = hashedPassword.split("\\" + SEPARATOR, 2);
            if (parts.length != 2) {
                return false;
            }
            
            String saltBase64 = parts[0];
            String storedHash = parts[1];
            
            // salt 디코딩
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            
            // 입력된 패스워드를 같은 방식으로 해싱
            String saltedPassword = rawPassword + Base64.getEncoder().encodeToString(salt);
            String inputHash = Hashing.sha256()
                .hashString(saltedPassword, StandardCharsets.UTF_8)
                .toString();
            
            // 해시 비교
            return storedHash.equals(inputHash);
            
        } catch (Exception e) {
            return false;
        }
    }
}