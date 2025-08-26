package com.puppytalk.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SHA-256 기반 비밀번호 암호화 구현체
 * 
 * Salt와 반복 해싱을 사용하여 보안을 강화한 SHA-256 암호화를 제공합니다.
 * 타이밍 공격을 방지하기 위해 상수 시간 비교를 사용합니다.
 */
public final class SHA256PasswordEncoder implements PasswordEncoder {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 32;
    private static final int HASH_ITERATIONS = 10000;
    private static final String SEPARATOR = ":";
    
    private final SecureRandom secureRandom;
    private final MessageDigest messageDigest;
    
    /**
     * 기본 생성자
     * SecureRandom과 MessageDigest를 초기화합니다.
     * 
     * @throws RuntimeException MessageDigest 초기화 실패 시
     */
    public SHA256PasswordEncoder() {
        this.secureRandom = new SecureRandom();
        try {
            this.messageDigest = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다", e);
        }
    }
    
    @Override
    public String encode(String rawPassword) {
        validateRawPassword(rawPassword);
        
        byte[] salt = generateSalt();
        byte[] hashedPassword = hashPassword(rawPassword, salt, HASH_ITERATIONS);
        
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
        
        return saltBase64 + SEPARATOR + hashBase64 + SEPARATOR + HASH_ITERATIONS;
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            String[] parts = encodedPassword.split("\\" + SEPARATOR);
            if (parts.length != 3) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            int iterations = Integer.parseInt(parts[2]);
            
            byte[] actualHash = hashPassword(rawPassword, salt, iterations);
            
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }
    
    /**
     * 평문 비밀번호 유효성 검증
     * 
     * @param rawPassword 평문 비밀번호
     * @throws IllegalArgumentException 비밀번호가 유효하지 않은 경우
     */
    private void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
    }
    
    /**
     * 안전한 랜덤 salt 생성
     * 
     * @return 생성된 salt 바이트 배열
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    /**
     * 비밀번호를 해싱합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @param salt salt 바이트 배열
     * @param iterations 반복 횟수
     * @return 해싱된 비밀번호 바이트 배열
     */
    private byte[] hashPassword(String rawPassword, byte[] salt, int iterations) {
        messageDigest.reset();
        messageDigest.update(salt);
        
        byte[] hash = messageDigest.digest(rawPassword.getBytes());
        
        for (int i = 1; i < iterations; i++) {
            messageDigest.reset();
            hash = messageDigest.digest(hash);
        }
        
        return hash;
    }
    
    /**
     * 타이밍 공격을 방지하기 위한 상수 시간 비교
     * 
     * @param expected 예상 바이트 배열
     * @param actual 실제 바이트 배열
     * @return 두 배열이 같으면 true, 다르면 false
     */
    private boolean constantTimeEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < expected.length; i++) {
            result |= expected[i] ^ actual[i];
        }
        
        return result == 0;
    }
}