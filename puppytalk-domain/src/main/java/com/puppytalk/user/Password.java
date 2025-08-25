package com.puppytalk.user;

import java.util.Objects;

/**
 * 비밀번호 값 객체
 * 
 * 사용자 비밀번호를 안전하게 암호화하고 검증하는 값 객체입니다.
 * 전략 패턴을 사용하여 다양한 암호화 알고리즘을 지원할 수 있도록 설계되었습니다.
 * 
 * 확장성:
 * - 새로운 암호화 알고리즘 추가 시 PasswordEncoder 인터페이스를 구현하면 됩니다.
 * - 기존 코드 수정 없이 암호화 방식을 변경할 수 있습니다.
 * - 다양한 보안 요구사항에 맞춰 암호화 정책을 조정할 수 있습니다.
 */
public final class Password {
    
    private static final PasswordEncoder DEFAULT_ENCODER = new SHA256PasswordEncoder();
    
    private final String encryptedValue;
    private final PasswordEncoder encoder;
    
    private Password(String encryptedValue, PasswordEncoder encoder) {
        if (encryptedValue == null || encryptedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("암호화된 비밀번호는 필수입니다");
        }
        if (encoder == null) {
            throw new IllegalArgumentException("PasswordEncoder는 필수입니다");
        }
        
        this.encryptedValue = encryptedValue;
        this.encoder = encoder;
    }
    
    /**
     * 평문 비밀번호로부터 Password 객체를 생성합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @return 암호화된 Password 객체
     * @throws IllegalArgumentException 비밀번호가 null이거나 빈 문자열인 경우
     */
    public static Password fromRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }
        
        String encryptedValue = encrypt(rawPassword);
        return new Password(encryptedValue);
    }
    
    /**
     * 이미 암호화된 비밀번호로부터 Password 객체를 생성합니다.
     * 
     * @param encryptedPassword 암호화된 비밀번호
     * @return Password 객체
     * @throws IllegalArgumentException 암호화된 비밀번호가 null이거나 빈 문자열인 경우
     */
    public static Password fromEncryptedPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("암호화된 비밀번호는 필수입니다");
        }
        
        return new Password(encryptedPassword);
    }
    
    /**
     * 평문 비밀번호와 일치하는지 검증합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @return 비밀번호가 일치하면 true, 그렇지 않으면 false
     */
    public boolean matches(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        
        try {
            byte[] saltAndHash = Base64.getDecoder().decode(encryptedValue);
            
            if (saltAndHash.length < SALT_LENGTH) {
                return false;
            }
            
            // salt 추출
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(saltAndHash, 0, salt, 0, SALT_LENGTH);
            
            // 저장된 해시 추출
            byte[] storedHash = new byte[saltAndHash.length - SALT_LENGTH];
            System.arraycopy(saltAndHash, SALT_LENGTH, storedHash, 0, storedHash.length);
            
            // 입력된 비밀번호로 해시 생성
            byte[] inputHash = hashPassword(rawPassword, salt);
            
            // 바이트 배열 비교 (타이밍 공격 방지)
            return constantTimeEquals(storedHash, inputHash);
            
        } catch (IllegalArgumentException e) {
            // Base64 디코딩 실패 등
            return false;
        }
    }
    
    /**
     * 새로운 비밀번호로 변경합니다.
     * 
     * @param newRawPassword 새로운 평문 비밀번호
     * @return 새로운 Password 객체
     */
    public Password change(String newRawPassword) {
        return fromRawPassword(newRawPassword);
    }
    
    /**
     * 암호화된 비밀번호 값을 반환합니다.
     * 
     * @return 암호화된 비밀번호
     */
    public String value() {
        return encryptedValue;
    }
    
    /**
     * 비밀번호를 암호화합니다.
     */
    private static String encrypt(String rawPassword) {
        byte[] salt = generateSalt();
        byte[] hashedPassword = hashPassword(rawPassword, salt);
        
        // salt + hash를 Base64로 인코딩하여 저장
        byte[] saltAndHash = new byte[salt.length + hashedPassword.length];
        System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
        System.arraycopy(hashedPassword, 0, saltAndHash, salt.length, hashedPassword.length);
        
        return Base64.getEncoder().encodeToString(saltAndHash);
    }
    
    /**
     * 랜덤한 Salt를 생성합니다.
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * 비밀번호와 Salt를 사용하여 해시를 생성합니다.
     */
    private static byte[] hashPassword(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // PBKDF2와 유사한 방식으로 반복 해싱
            byte[] hash = password.getBytes(StandardCharsets.UTF_8);
            
            for (int i = 0; i < HASH_ITERATIONS; i++) {
                digest.reset();
                digest.update(salt);
                hash = digest.digest(hash);
            }
            
            return hash;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다", e);
        }
    }
    
    /**
     * 타이밍 공격을 방지하는 바이트 배열 비교
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Password other)) return false;
        return Objects.equals(encryptedValue, other.encryptedValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(encryptedValue);
    }
    
    @Override
    public String toString() {
        return "Password{***}"; // 보안을 위해 실제 값은 숨김
    }
}