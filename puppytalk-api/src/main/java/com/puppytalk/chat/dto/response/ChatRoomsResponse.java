package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.dto.response.ChatRoomListResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 채팅방 목록 응답
 */
@Schema(description = "채팅방 목록 응답")
public record ChatRoomsResponse(
    
    @Schema(description = "채팅방 목록")
    List<ChatRoomResponse> chatRoomList,
    
    @Schema(description = "전체 개수", example = "5")
    int totalCount
) {
    
    public static ChatRoomsResponse from(ChatRoomListResult result) {
        List<ChatRoomResponse> responses = result.chatRoomList().stream()
                .map(ChatRoomResponse::from)
                .toList();
                
        return new ChatRoomsResponse(responses, result.totalCount());
    }
}