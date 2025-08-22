package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.MessageType;
import com.puppytalk.chat.dto.response.MessageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 메시지 응답
 */
@Schema(description = "메시지 응답")
public record MessageResponse(
    
    @Schema(description = "메시지 ID", example = "1")
    Long messageId,
    
    @Schema(description = "채팅방 ID", example = "1")
    Long chatRoomId,
    
    @Schema(description = "메시지 타입", example = "USER")
    MessageType type,
    
    @Schema(description = "메시지 내용", example = "안녕하세요!")
    String content,
    
    @Schema(description = "생성 시각", example = "2023-12-01T15:30:00")
    LocalDateTime createdAt
) {
    
    public static MessageResponse from(MessageResult result) {
        return new MessageResponse(
            result.messageId(),
            result.chatRoomId(),
            result.type(),
            result.content(),
            result.createdAt()
        );
    }
}