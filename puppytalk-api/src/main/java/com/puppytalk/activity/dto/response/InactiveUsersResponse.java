package com.puppytalk.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 비활성 사용자 목록 응답 DTO
 */
@Schema(description = "비활성 사용자 목록 응답")
public record InactiveUsersResponse(
    
    @Schema(description = "비활성 사용자 ID 목록")
    List<Long> userIds,
    
    @Schema(description = "비활성 사용자 수", example = "5")
    int count
) {
    
    /**
     * InactiveUsersResult로부터 응답 DTO 생성
     */
    public static InactiveUsersResponse from(InactiveUsersResult result) {
        return new InactiveUsersResponse(
            result.userIds(),
            result.userIds().size()
        );
    }
}
