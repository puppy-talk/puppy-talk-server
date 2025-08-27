package com.puppytalk.activity;

/**
 * 활동 ID 값 객체
 */
public class ActivityId {

    private final Long value;

    private ActivityId(Long value) {
        this.value = value;
    }

    /**
     * 하나의 매개변수를 받아 타입 변환 (데이터베이스에서 조회된 값용)
     */
    public static ActivityId from(Long value) {
        return new ActivityId(value);
    }

    /**
     * 항상 새로운 인스턴스를 생성해 반환 (신규 생성용)
     */
    public static ActivityId create() {
        return new ActivityId(null);
    }

    /**
     * ID가 저장된 상태인지 확인
     */
    public boolean isStored() {
        return value != null && value > 0;
    }

    /**
     * JPA 호환성을 위한 값 접근
     */
    public Long getValue() {
        return value;
    }
}