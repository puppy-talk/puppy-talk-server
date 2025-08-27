package com.puppytalk.activity;

import com.puppytalk.user.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 활동 리포지토리 구현
 * 
 * Backend 관점: 높은 처리량과 안정성을 위한 최적화
 */
@Repository
public class UserActivityRepositoryImpl implements UserActivityRepository {
    
    private final UserActivityJpaRepository jpaRepository;
    
    public UserActivityRepositoryImpl(UserActivityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public ActivityId save(UserActivity activity) {
        Assert.notNull(activity, "UserActivity must not be null");
        
        UserActivityJpaEntity entity = UserActivityJpaEntity.from(activity);
        UserActivityJpaEntity saved = jpaRepository.save(entity);
        return ActivityId.from(saved.getId());
    }
    
    @Override
    public Optional<UserActivity> findById(ActivityId id) {
        Assert.notNull(id, "ActivityId must not be null");
        
        if (!id.isStored()) {
            return Optional.empty();
        }
        
        return jpaRepository.findById(id.getValue())
            .map(UserActivityJpaEntity::toDomain);
    }
    
    @Override
    public List<UserId> findInactiveUserIds(LocalDateTime inactivityThreshold) {
        Assert.notNull(inactivityThreshold, "Inactivity threshold must not be null");
        
        return jpaRepository.findInactiveUserIds(inactivityThreshold)
            .stream()
            .map(UserId::from)
            .toList();
    }
}