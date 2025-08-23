package com.puppytalk.user;

import com.puppytalk.infrastructure.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;
    
    protected UserJpaEntity() {
        // JPA 전용 기본 생성자
    }
    
    private UserJpaEntity(Long id, String username, String email, UserStatus status,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 도메인 객체로부터 JPA 엔티티 생성
     */
    public static UserJpaEntity fromDomain(User user) {
        return new UserJpaEntity(
            user.id().isStored() ? user.id().value() : null,
            user.username(),
            user.email(),
            user.status(),
            user.createdAt(),
            LocalDateTime.now()
        );
    }
    
    /**
     * JPA 엔티티를 도메인 객체로 변환
     */
    public User toDomain() {
        return User.of(
            UserId.of(this.id),
            this.username,
            this.email,
            this.createdAt,
            this.status
        );
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    
    // Setters for JPA updates
    public void updateFromDomain(User user) {
        this.username = user.username();
        this.email = user.email();
        this.status = user.status();
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