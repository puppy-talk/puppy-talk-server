package com.puppytalk.pet.dto.request;

/**
 * 반려동물 생성 커맨드
 */
public record PetCreateCommand(
    Long ownerId,
    String petName,
    Long personaId
) {
    /**
     * API 요청 파라미터로부터 커맨드 생성
     */
    public static PetCreateCommand of(Long ownerId, String name, Long personaId) {
        return new PetCreateCommand(ownerId, name, personaId);
    }
}