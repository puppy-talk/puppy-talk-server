package com.puppytalk.user;

import com.puppytalk.infrastructure.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
public class UserJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "password", nullable = false, length = 500)
    private String password;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
    
    protected UserJpaEntity() {
        // JPA 전용 기본 생성자
    }
    
    private UserJpaEntity(Long id, String username, String email, String password, boolean isDeleted,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * model -> jpa entity
     */
    public static UserJpaEntity from(User user) {
        return new UserJpaEntity(
            user.id() != null ? user.id().value() : null,
            user.username(),
            user.email(),
            user.password(),
            user.isDeleted(),
            user.createdAt(),
            LocalDateTime.now()
        );
    }
    
    /**
     * jpa entity -> model
     */
    public User toDomain() {
        return User.of(
            UserId.from(this.id),
            this.username,
            this.email,
            this.password,
            this.createdAt,
            this.updatedAt,
            this.isDeleted
        );
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isDeleted() { return isDeleted; }
    
    /**
     * updateFromDomain 패턴 - 도메인 객체로부터 일괄 업데이트
     * 개별 setter 사용을 방지하여 불변성 보장
     */
    public void update(User user) {
        this.username = user.username();
        this.email = user.email();
        this.password = user.password();
        this.isDeleted = user.isDeleted();
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserJpaEntity other)) return false;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}