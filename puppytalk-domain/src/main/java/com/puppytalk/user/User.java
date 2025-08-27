package com.puppytalk.user;

import com.puppytalk.support.validation.Preconditions;
import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_EMAIL_LENGTH = 100;


    private final UserId id;
    private final String username;
    private final String email;
    private final String password;
    private final PasswordEncoder passwordEncoder;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean isDeleted;

    private User(UserId id, String username, String email, String password,
        PasswordEncoder passwordEncoder,
        LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordEncoder = passwordEncoder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    /**
     * 새 사용자 생성
     */
    public static User create(String username, String email, String encryptedPassword) {
        Preconditions.requireNonBlank(username, "Username", MAX_USERNAME_LENGTH);
        Preconditions.requireNonBlank(email, "Email", MAX_EMAIL_LENGTH);
        Preconditions.requireNonBlank(encryptedPassword, "Encrypted Password");

        String validUsername = username.trim();
        String validEmail = email.trim();
        LocalDateTime now = LocalDateTime.now();
        return new User(null, validUsername, validEmail, encryptedPassword, new SHA256PasswordEncoder(), now,
            now, false);
    }

    /**
     * 기존 사용자 데이터로부터 객체 생성
     */
    public static User of(UserId id, String username, String email, String encryptedPassword,
        LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted) {
        return of(id, username, email, encryptedPassword, new SHA256PasswordEncoder(),
            createdAt, updatedAt, isDeleted);
    }

    /**
     * 기존 사용자 데이터로부터 객체 생성 (암호화 방식 지정)
     */
    public static User of(UserId id, String username, String email, String encryptedPassword,
        PasswordEncoder passwordEncoder,
        LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted) {
        Preconditions.requireValidId(id, "UserId");
        Preconditions.requireNonBlank(username, "Username", MAX_USERNAME_LENGTH);
        Preconditions.requireNonBlank(email, "Email", MAX_EMAIL_LENGTH);
        Preconditions.requireNonBlank(encryptedPassword, "Password");
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt must not be null");
        }

        String validUsername = username.trim();
        String validEmail = email.trim();
        return new User(id, validUsername, validEmail, encryptedPassword, passwordEncoder,
            createdAt, updatedAt, isDeleted);
    }


    /**
     * 이메일 변경
     */
    public User withEmail(String newEmail) {
        Preconditions.requireNonBlank(newEmail, "Email", MAX_EMAIL_LENGTH);
        String validEmail = newEmail.trim();
        return new User(this.id, this.username, validEmail, this.password, this.passwordEncoder,
            this.createdAt, LocalDateTime.now(), this.isDeleted);
    }

    /**
     * 비밀번호 검증
     *
     * @param rawPassword 평문 비밀번호
     * @return 비밀번호가 일치하면 true, 그렇지 않으면 false
     */
    public boolean checkPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, password);
    }

    /**
     * 비밀번호 변경
     *
     * @param newRawPassword 새로운 평문 비밀번호
     * @return 비밀번호가 변경된 새로운 User 인스턴스
     */
    public User changePassword(String newRawPassword) {
        Preconditions.requireNonBlank(newRawPassword, "Password");
        String newEncryptedPassword = passwordEncoder.encode(newRawPassword);
        return new User(id, username, email, newEncryptedPassword, passwordEncoder, createdAt,
            updatedAt, isDeleted);
    }

    /**
     * 사용자 삭제
     */
    public User withDeletedStatus() {
        return new User(id, username, email, password, passwordEncoder, createdAt, updatedAt, true);
    }


    // getter
    public UserId id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
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