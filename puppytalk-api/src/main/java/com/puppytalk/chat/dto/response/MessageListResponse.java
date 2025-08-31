package com.puppytalk.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 메시지 목록 응답 (커서 기반 페이징)
 */
@Schema(description = "메시지 목록 응답")
public record MessageListResponse(
    
    @Schema(description = "메시지 목록")
    List<MessageResponse> messageList,
    
    @Schema(description = "조회된 메시지 개수", example = "25")
    int count,
    
    @Schema(description = "다음 페이지 커서 (다음 조회 시 사용)", example = "123")
    Long nextCursor,
    
    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {
    
    public static MessageListResponse from(MessageListResult result) {
        List<MessageResponse> responses = result.messageList().stream()
                .map(MessageResponse::from)
                .toList();
                
        return new MessageListResponse(
            responses, 
            result.count(), 
            result.nextCursor(), 
            result.hasNext()
        );
    }
}