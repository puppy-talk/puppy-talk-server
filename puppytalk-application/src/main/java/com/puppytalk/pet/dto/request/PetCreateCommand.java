package com.puppytalk.pet.dto.request;

import org.springframework.util.Assert;

/**
 * 반려동물 생성 커맨드
 */
public record PetCreateCommand(
    Long ownerId,
    String petName
) {
    public PetCreateCommand {
        Assert.notNull(ownerId, "소유자 ID는 필수입니다");
        Assert.hasText(petName, "반려동물 이름은 필수입니다");
    }
    
    /**
     * API 요청 파라미터로부터 커맨드 생성
     */
    public static PetCreateCommand of(Long ownerId, String petName) {
        return new PetCreateCommand(ownerId, petName);
    }
}