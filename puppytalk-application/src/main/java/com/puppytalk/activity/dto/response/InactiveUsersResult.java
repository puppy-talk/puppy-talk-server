package com.puppytalk.activity.dto.response;

import java.util.ArrayList;
import java.util.List;

/**
 * 비활성 사용자 목록 결과
 */
public record InactiveUsersResult(
    List<Long> userIds,
    int totalCount
) {
    
    public InactiveUsersResult {
        // 방어적 복사 및 불변 리스트 생성
        userIds = userIds != null ? List.copyOf(userIds) : List.of();
        totalCount = userIds.size();
    }
    
    public static InactiveUsersResult from(List<Long> userIds) {
        return new InactiveUsersResult(userIds, userIds != null ? userIds.size() : 0);
    }
    
    public static InactiveUsersResult empty() {
        return new InactiveUsersResult(List.of(), 0);
    }
}