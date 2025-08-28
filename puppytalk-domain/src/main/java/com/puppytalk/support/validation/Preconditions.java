package com.puppytalk.support.validation;

import com.puppytalk.support.EntityId;

public final class Preconditions {

    private Preconditions() {}

    /**
     * 값이 null이거나 공백이면 IllegalArgumentException 발생
     *
     * @param value 검사할 값
     * @param name 파라미터 이름 (에러 메시지용)
     * @throws IllegalArgumentException 값이 null이거나 공백인 경우
     */
    public static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
    }

    /**
     * 값이 null이거나 공백, 최대 길이를 초과하면 IllegalArgumentException 발생
     *
     * @param value     검사할 값
     * @param name      파라미터 이름 (에러 메시지용)
     * @param maxLength 허용 최대 길이
     * @throws IllegalArgumentException 값이 null이거나 공백이거나 길이 초과인 경우
     */
    public static void requireNonBlank(String value, String name, int maxLength) {
        requireNonBlank(value, name);
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(name + " length must be ≤ " + maxLength + ", but was " + value.length());
        }
    }


    /**
     * 객체가 null이 아님을 검증
     *
     * @param obj 검사할 객체
     * @param name 파라미터 이름
     * @param <T> 객체 타입
     * @return 검증된 객체
     * @throws IllegalArgumentException 객체가 null인 경우
     */
    public static <T> T requireNonNull(T obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return obj;
    }

    /**
     * EntityId가 null이거나 저장되지 않은 상태면 IllegalArgumentException을 던진다.
     *
     * @param entityId 검사할 EntityId
     * @param name     파라미터 이름 (에러 메시지용)
     * @throws IllegalArgumentException EntityId가 null이거나 저장되지 않은 경우
     */
    public static void requireValidId(EntityId entityId, String name) {
        if (entityId == null || !entityId.isStored()) {
            throw new IllegalArgumentException(name + " must be a valid stored ID");
        }
    }
}
