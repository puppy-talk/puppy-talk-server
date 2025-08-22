package com.puppytalk.user;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소 JPA 구현체
 */
@Repository
@Transactional(readOnly = true)
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }
    
    @Override
    @Transactional
    public UserId save(User user) {
        if (user.getId().isStored()) {
            // 기존 사용자 업데이트
            UserJpaEntity existingEntity = userJpaRepository.findById(user.getId().value())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + user.getId().value()));
            
            existingEntity.updateFromDomain(user);
            userJpaRepository.save(existingEntity);
            return user.getId();
        } else {
            // 새로운 사용자 생성
            UserJpaEntity entity = UserJpaEntity.fromDomain(user);
            UserJpaEntity savedEntity = userJpaRepository.save(entity);
            return UserId.of(savedEntity.getId());
        }
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        return userJpaRepository.findById(userId.value())
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
            .map(UserJpaEntity::toDomain);
    }
    
    @Override
    public List<User> findByStatus(UserStatus status) {
        return userJpaRepository.findByStatus(status).stream()
            .map(UserJpaEntity::toDomain)
            .toList();
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}