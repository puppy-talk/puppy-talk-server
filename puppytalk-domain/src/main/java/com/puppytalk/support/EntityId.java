package com.puppytalk.support;

import java.util.Objects;

/**
 * 모든 도메인 ID 클래스의 상위 클래스
 * ID의 공통적인 특성과 동작을 정의합니다.
 */
public abstract class EntityId {
    
    protected final Long value;
    
    protected EntityId(Long value) {
        this.value = value;
    }
    
    /**
     * ID 값이 저장된 상태인지 확인 (null이 아니고 0보다 큰 값)
     */
    public boolean isStored() {
        return value != null && value > 0;
    }

    /**
     * ID 값을 반환
     */
    public Long getValue() {
        return value;
    }
    
    /**
     * ID 값을 반환 (축약형)
     */
    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EntityId entityId = (EntityId) obj;
        return Objects.equals(value, entityId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + value + "}";
    }
}
