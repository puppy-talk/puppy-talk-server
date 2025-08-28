package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.dto.response.ChatRoomResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 채팅방 생성/조회 응답
 */
@Schema(description = "채팅방 생성/조회 응답")
public record ChatRoomResponse(
    
    @Schema(description = "채팅방 ID", example = "1")
    Long chatRoomId,
    
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "반려동물 ID", example = "1")
    Long petId,
    
    @Schema(description = "새로 생성된 채팅방 여부", example = "true")
    Boolean isNewlyCreated,
    
    @Schema(description = "생성 시각", example = "2023-12-01T15:30:00")
    LocalDateTime createdAt,
    
    @Schema(description = "마지막 메시지 시각", example = "2023-12-01T15:30:00")
    LocalDateTime lastMessageAt
) {
    
    /**
     * ChatRoom 도메인 객체로부터 응답 생성
     */
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
            chatRoom.id().getValue(),
            chatRoom.userId().getValue(),
            chatRoom.petId().getValue(),
            null, // isNewlyCreated는 Application Layer에서 결정
            chatRoom.createdAt(),
            chatRoom.lastMessageAt()
        );
    }
    
    
    /**
     * ChatRoomResult로부터 응답 생성
     */
    public static ChatRoomResponse from(ChatRoomResult result) {
        return new ChatRoomResponse(
            result.chatRoomId(),
            result.userId(),
            result.petId(),
            null, // isCreated not available in ChatRoomResult
            result.createdAt(),
            result.lastMessageAt()
        );
    }
}