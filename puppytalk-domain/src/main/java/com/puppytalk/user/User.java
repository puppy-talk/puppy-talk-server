package com.puppytalk.user;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    /**
     * 휴면 계정 판단 기준 (일)
     */
    public static final int DORMANT_DAYS = 28;

    private final UserId id;
    private final String username;
    private final String email;
    private final String password;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime lastActiveAt;
    private final boolean isDeleted;

    private User(UserId id, String username, String email, String password,
        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastActiveAt, boolean isDeleted) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastActiveAt = lastActiveAt;
        this.isDeleted = isDeleted;
    }

    /**
     * 새 사용자 생성
     */
    public static User create(String username, String email, String encryptedPassword) {
        LocalDateTime now = LocalDateTime.now();
        return new User(null, username.trim(), email.trim(), encryptedPassword, now, now, now, false);
    }

    /**
     * 기존 사용자 데이터로부터 객체 생성
     */
    public static User of(UserId id, String username, String email, String encryptedPassword,
        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastActiveAt, boolean isDeleted) {
        return new User(id, username, email, encryptedPassword, createdAt, updatedAt, lastActiveAt, isDeleted);
    }


    /**
     * 이메일 변경
     */
    public User withEmail(String newEmail) {
        return new User(this.id, this.username, newEmail.trim(), this.password,
            this.createdAt, LocalDateTime.now(), this.lastActiveAt, this.isDeleted);
    }

    /**
     * 비밀번호 변경 (이미 암호화된 비밀번호)
     */
    public User withPassword(String newEncryptedPassword) {
        return new User(id, username, email, newEncryptedPassword, createdAt,
            LocalDateTime.now(), lastActiveAt, isDeleted);
    }

    /**
     * 사용자 삭제
     */
    public User withDeletedStatus() {
        return new User(id, username, email, password, createdAt, LocalDateTime.now(), lastActiveAt, true);
    }

    /**
     * 사용자 활동 시간 업데이트
     */
    public User updateLastActiveTime() {
        return new User(id, username, email, password, createdAt, LocalDateTime.now(), LocalDateTime.now(), isDeleted);
    }

    /**
     * 현재 사용자 상태 계산
     */
    public UserStatus getCurrentStatus() {
        if (isDeleted) {
            return UserStatus.DELETED;
        }
        
        if (isDormant()) {
            return UserStatus.DORMANT;
        }
        
        return UserStatus.ACTIVE;
    }

    /**
     * 휴면 계정 여부 확인
     */
    public boolean isDormant() {
        if (lastActiveAt == null) {
            return false;
        }
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DORMANT_DAYS);
        return lastActiveAt.isBefore(cutoffDate);
    }

    /**
     * 알림 수신 가능 여부 확인
     */
    public boolean canReceiveNotifications() {
        return getCurrentStatus().canReceiveNotifications();
    }


    // getter
    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + getId() +
            ", username='" + getUsername() + '\'' +
            ", email='" + getEmail() + '\'' +
            ", isDeleted=" + isDeleted() +
            '}';
    }
}