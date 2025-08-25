package com.puppytalk.user;

/**
 * 비밀번호 암호화 인터페이스
 * 
 * 다양한 암호화 알고리즘을 지원하기 위한 추상화 계층입니다.
 * 새로운 암호화 방식을 추가할 때는 이 인터페이스를 구현하면 됩니다.
 */
public interface PasswordEncoder {
    
    /**
     * 평문 비밀번호를 암호화합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @return 암호화된 비밀번호
     * @throws IllegalArgumentException 비밀번호가 유효하지 않은 경우
     */
    String encode(String rawPassword);
    
    /**
     * 평문 비밀번호와 암호화된 비밀번호가 일치하는지 확인합니다.
     * 
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치하면 true, 그렇지 않으면 false
     */
    boolean matches(String rawPassword, String encodedPassword);
    
    /**
     * 암호화 알고리즘의 식별자를 반환합니다.
     * 
     * @return 알고리즘 식별자 (예: "SHA256", "BCrypt" 등)
     */
    String getAlgorithm();
}