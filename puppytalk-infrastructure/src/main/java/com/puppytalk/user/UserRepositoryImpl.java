package com.puppytalk.user;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소 JPA 구현체
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        if (userJpaRepository == null) {
            throw new IllegalArgumentException("UserJpaRepository must not be null");
        }
        this.userJpaRepository = userJpaRepository;
    }
    
    @Override
    public UserId save(User user) {
        if (user.id() != null && user.id().isStored()) {
            // 기존 사용자 업데이트
            UserJpaEntity existingEntity = userJpaRepository.findById(user.id().value())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + user.id().value()));
            
            existingEntity.update(user);
            userJpaRepository.save(existingEntity);
            return user.id();
        } else {
            // 새로운 사용자 생성
            UserJpaEntity entity = UserJpaEntity.from(user);
            UserJpaEntity savedEntity = userJpaRepository.save(entity);
            return UserId.from(savedEntity.getId());
        }
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        Assert.notNull(userId, "UserId must not be null");
        
        if (!userId.isStored()) {
            return Optional.empty();
        }
        
        return userJpaRepository.findById(userId.value())
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        Assert.hasText(username, "Username must not be null or empty");
        
        return userJpaRepository.findByUsername(username)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        Assert.hasText(email, "Email must not be null or empty");
        
        return userJpaRepository.findByEmail(email)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public List<User> findActiveUsers() {
        return userJpaRepository.findByIsDeletedFalse().stream()
            .map(UserJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public List<User> findDeletedUsers() {
        return userJpaRepository.findByIsDeletedTrue().stream()
            .map(UserJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public boolean existsByUsername(String username) {
        Assert.hasText(username, "Username must not be null or empty");
        
        return userJpaRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        Assert.hasText(email, "Email must not be null or empty");
        
        return userJpaRepository.existsByEmail(email);
    }
}