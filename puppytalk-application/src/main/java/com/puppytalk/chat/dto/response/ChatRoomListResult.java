package com.puppytalk.chat.dto.response;

import com.puppytalk.chat.ChatRoom;
import java.util.List;

/**
 * 채팅방 목록 조회 결과
 */
public record ChatRoomListResult(
    List<ChatRoomResult> chatRoomList,
    int totalCount
) {
    
    public static ChatRoomListResult from(List<ChatRoom> chatRooms) {
        List<ChatRoomResult> results = chatRooms.stream()
                .map(ChatRoomResult::from)
                .toList();
                
        return new ChatRoomListResult(results, results.size());
    }
    
    /**
     * 빈 결과 생성
     */
    public static ChatRoomListResult empty() {
        return new ChatRoomListResult(List.of(), 0);
    }
}