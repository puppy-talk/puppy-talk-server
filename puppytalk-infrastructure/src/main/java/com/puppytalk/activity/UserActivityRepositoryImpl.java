package com.puppytalk.activity;

import com.puppytalk.user.UserId;
import org.springframework.stereotype.Repository;

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
        UserActivityJpaEntity entity = UserActivityJpaEntity.fromDomain(activity);
        UserActivityJpaEntity saved = jpaRepository.save(entity);
        return ActivityId.from(saved.getId());
    }
    
    @Override
    public Optional<UserActivity> findById(ActivityId id) {
        return jpaRepository.findById(id.getValue())
            .map(UserActivityJpaEntity::toDomain);
    }
    
    @Override
    public List<UserId> findInactiveUserIds(LocalDateTime inactivityThreshold) {
        return jpaRepository.findInactiveUserIds(inactivityThreshold)
            .stream()
            .map(UserId::from)
            .toList();
    }
}