package com.puppytalk.user;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 사용자 저장소 구현체 (임시 인메모리 구현)
 * TODO: 실제 JPA 구현으로 교체 필요
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final ConcurrentHashMap<Long, User> userStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> usernameIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> emailIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    @Override
    public UserId save(User user) {
        if (user.getId().value() == null) {
            // 새로운 사용자
            Long newId = idGenerator.getAndIncrement();
            UserId userId = UserId.of(newId);
            
            User savedUser = User.restore(
                userId,
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getStatus()
            );
            
            userStore.put(newId, savedUser);
            usernameIndex.put(user.getUsername(), newId);
            emailIndex.put(user.getEmail(), newId);
            
            return userId;
        } else {
            // 기존 사용자 업데이트
            userStore.put(user.getId().value(), user);
            return user.getId();
        }
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        return Optional.ofNullable(userStore.get(userId.value()));
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        Long userId = usernameIndex.get(username);
        return userId != null ? Optional.ofNullable(userStore.get(userId)) : Optional.empty();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        Long userId = emailIndex.get(email);
        return userId != null ? Optional.ofNullable(userStore.get(userId)) : Optional.empty();
    }
    
    @Override
    public List<User> findByStatus(UserStatus status) {
        return userStore.values().stream()
            .filter(user -> user.getStatus() == status)
            .toList();
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return usernameIndex.containsKey(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }
}