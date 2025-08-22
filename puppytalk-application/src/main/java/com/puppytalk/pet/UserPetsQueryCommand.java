package com.puppytalk.pet;

/**
 * 사용자 반려동물 목록 조회 커맨드
 */
public record UserPetsQueryCommand(
    Long ownerId
) {
    public UserPetsQueryCommand {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID는 필수이며 양수여야 합니다");
        }
    }
    
    /**
     * API 요청으로부터 커맨드 생성
     */
    public static UserPetsQueryCommand of(Long ownerId) {
        return new UserPetsQueryCommand(ownerId);
    }
}