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
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new IllegalArgumentException("암호화된 비밀번호는 필수입니다");
        }
        if (encoder == null) {
            throw new IllegalArgumentException("PasswordEncoder는 필수입니다");
        }
        
        this.encryptedValue = encryptedValue;
        this.encoder = encoder;
    }
    
    /**
     * 기본 암호화 방식(SHA-256)으로 평문 비밀번호로부터 Password 객체를 생성합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @return Password 객체
     * @throws IllegalArgumentException 비밀번호가 유효하지 않은 경우
     */
    public static Password fromRawPassword(String rawPassword) {
        return fromRawPassword(rawPassword, DEFAULT_ENCODER);
    }
    
    /**
     * 지정된 암호화 방식으로 평문 비밀번호로부터 Password 객체를 생성합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @param encoder 사용할 암호화 인코더
     * @return Password 객체
     * @throws IllegalArgumentException 비밀번호나 인코더가 유효하지 않은 경우
     */
    public static Password fromRawPassword(String rawPassword, PasswordEncoder encoder) {
        if (encoder == null) {
            throw new IllegalArgumentException("PasswordEncoder는 필수입니다");
        }
        
        String encryptedValue = encoder.encode(rawPassword);
        return new Password(encryptedValue, encoder);
    }
    
    /**
     * 이미 암호화된 비밀번호로부터 Password 객체를 생성합니다.
     * 기본 암호화 방식(SHA-256)을 사용합니다.
     * 
     * @param encryptedPassword 암호화된 비밀번호
     * @return Password 객체
     * @throws IllegalArgumentException 암호화된 비밀번호가 유효하지 않은 경우
     */
    public static Password fromEncryptedPassword(String encryptedPassword) {
        return fromEncryptedPassword(encryptedPassword, DEFAULT_ENCODER);
    }
    
    /**
     * 이미 암호화된 비밀번호로부터 Password 객체를 생성합니다.
     * 
     * @param encryptedPassword 암호화된 비밀번호
     * @param encoder 사용할 암호화 인코더
     * @return Password 객체
     * @throws IllegalArgumentException 암호화된 비밀번호나 인코더가 유효하지 않은 경우
     */
    public static Password fromEncryptedPassword(String encryptedPassword, PasswordEncoder encoder) {
        return new Password(encryptedPassword, encoder);
    }
    
    /**
     * 평문 비밀번호가 이 Password와 일치하는지 확인합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @return 일치하면 true, 그렇지 않으면 false
     */
    public boolean matches(String rawPassword) {
        return encoder.matches(rawPassword, encryptedValue);
    }
    
    /**
     * 비밀번호를 변경한 새로운 Password 객체를 생성합니다.
     * 동일한 암호화 방식을 사용합니다.
     * 
     * @param newRawPassword 새로운 평문 비밀번호
     * @return 새로운 Password 객체
     * @throws IllegalArgumentException 새 비밀번호가 유효하지 않은 경우
     */
    public Password change(String newRawPassword) {
        return fromRawPassword(newRawPassword, encoder);
    }
    
    /**
     * 다른 암호화 방식으로 비밀번호를 변경한 새로운 Password 객체를 생성합니다.
     * 
     * @param newRawPassword 새로운 평문 비밀번호
     * @param newEncoder 새로운 암호화 인코더
     * @return 새로운 Password 객체
     * @throws IllegalArgumentException 새 비밀번호나 인코더가 유효하지 않은 경우
     */
    public Password changeWithEncoder(String newRawPassword, PasswordEncoder newEncoder) {
        return fromRawPassword(newRawPassword, newEncoder);
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
     * 사용 중인 암호화 알고리즘 이름을 반환합니다.
     * 
     * @return 암호화 알고리즘 이름
     */
    public String getAlgorithm() {
        return encoder.getAlgorithm();
    }
    
    /**
     * 현재 사용 중인 PasswordEncoder를 반환합니다.
     * 
     * @return PasswordEncoder 인스턴스
     */
    public PasswordEncoder getEncoder() {
        return encoder;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Password other)) return false;
        return Objects.equals(encryptedValue, other.encryptedValue) &&
               Objects.equals(encoder.getClass(), other.encoder.getClass());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(encryptedValue, encoder.getClass());
    }
    
    @Override
    public String toString() {
        return "Password{algorithm=" + encoder.getAlgorithm() + ", value=***}";
    }
}