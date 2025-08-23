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
    private final boolean isDeleted;

    private User(UserId id, String username, String email, 
                 LocalDateTime createdAt, boolean isDeleted) {
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
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시각은 필수입니다");
        }

        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    public static User create(String username, String email) {
        return new User(
            null,
            username.trim(),
            email.trim().toLowerCase(),
            LocalDateTime.now(),
            false // 기본적으로 삭제되지 않은 상태
        );
    }

    public static User of(UserId id, String username, String email, 
                          LocalDateTime createdAt, boolean isDeleted) {
        if (id == null || !id.isStored()) {
            throw new IllegalArgumentException("저장된 사용자 ID가 필요합니다");
        }

        return new User(id, username, email, createdAt, isDeleted);
    }

    private static boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 &&
               email.length() <= 100;
    }

    /**
     * 사용자 삭제 (소프트 삭제)
     */
    public User withDeletedStatus() {
        return new User(id, username, email, createdAt, true);
    }

    /**
     * 사용자 복구 (삭제 취소)
     */
    public User withRestoredStatus() {
        return new User(id, username, email, createdAt, false);
    }

    /**
     * 사용자가 삭제되었는지 확인
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * 사용자가 활성 상태인지 확인 (삭제되지 않은 상태)
     */
    public boolean isActive() {
        return !isDeleted;
    }

    // getter
    public UserId id() { return id; }
    public String username() { return username; }
    public String email() { return email; }
    public LocalDateTime createdAt() { return createdAt; }

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
                ", isDeleted=" + isDeleted +
                '}';
    }
}