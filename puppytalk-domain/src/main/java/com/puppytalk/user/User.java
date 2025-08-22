package com.puppytalk.user;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자 엔티티
 * 
 * 시스템에 가입한 사용자를 나타내는 도메인 엔티티입니다.
 */
public class User {
    
    private final UserId id;
    private final String username;
    private final String email;
    private final LocalDateTime createdAt;
    private UserStatus status;
    
    private User(UserId id, String username, String email, 
                 LocalDateTime createdAt, UserStatus status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    /**
     * 새로운 사용자 생성 정적 팩토리 메서드
     */
    public static User create(String username, String email) {
        validateCreation(username, email);
        
        return new User(
            UserId.newUser(),
            username.trim(),
            email.trim().toLowerCase(),
            LocalDateTime.now(),
            UserStatus.ACTIVE
        );
    }
    
    /**
     * 기존 사용자 복원용 정적 팩토리 메서드 (Repository용)
     */
    public static User restore(UserId id, String username, String email, 
                             LocalDateTime createdAt, UserStatus status) {
        validateRestore(id, username, email, createdAt, status);
        
        return new User(id, username, email, createdAt, status);
    }
    
    private static void validateCreation(String username, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자명은 필수입니다");
        }
        if (username.trim().length() < 2 || username.trim().length() > 30) {
            throw new IllegalArgumentException("사용자명은 2-30자 사이여야 합니다");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }
    
    private static void validateRestore(UserId id, String username, String email, 
                                       LocalDateTime createdAt, UserStatus status) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 사용자 ID가 필요합니다");
        }
        validateCreation(username, email);
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("사용자 상태는 필수입니다");
        }
    }
    
    private static boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 &&
               email.length() <= 100;
    }
    
    /**
     * 사용자 비활성화 (소프트 삭제)
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    /**
     * 사용자 활성화
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * 사용자가 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    // Getters
    public UserId getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public UserStatus getStatus() { return status; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
}