package com.puppytalk.support.validation;

import com.puppytalk.support.EntityId;

/**
 * 문자열 및 공통 입력값 검증 유틸리티
 * 도메인 레이어에서 불변 조건을 간결하고 일관되게 보장하기 위해 사용한다.
 */
public final class Preconditions {

	private Preconditions() {
	}

	/**
	 * 값이 null이거나 공백이면 IllegalArgumentException 발생
	 *
	 * @param value 검사할 값
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
	 * @param maxLength 허용 최대 길이
	 */
	public static void requireNonBlank(String value, String name, int maxLength) {
		requireNonBlank(value, name);
		if (value.length() > maxLength) {
			throw new IllegalArgumentException(name + " length must be ≤ " + maxLength);
		}
	}

	/**
	 * EntityId가 null이거나 저장되지 않은 상태면 IllegalArgumentException을 던진다.
	 *
	 * @param entityId 검사할 EntityId
	 */
	public static void requireValidId(EntityId entityId) {
		if (entityId == null || !entityId.isStored()) {
			throw new IllegalArgumentException("Id must be a valid stored ID");
		}
	}

	/**
	 * EntityId가 null이거나 저장되지 않은 상태면 IllegalArgumentException을 던진다.
	 *
	 * @param entityId 검사할 EntityId
	 * @param name     파라미터 이름 (에러 메시지용)
	 */
	public static void requireValidId(EntityId entityId, String name) {
		if (entityId == null || !entityId.isStored()) {
			throw new IllegalArgumentException(name + " must be a valid stored ID");
		}
	}
}
