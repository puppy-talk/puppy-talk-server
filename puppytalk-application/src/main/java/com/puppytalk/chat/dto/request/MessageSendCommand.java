package com.puppytalk.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageSendCommand(
    @NotNull(message = "채팅방 ID는 필수입니다")
    Long chatRoomId,
    
    @NotNull(message = "사용자 ID는 필수입니다") 
    Long userId,
    
    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다")
    String content
) {
    public static MessageSendCommand of(Long chatRoomId, Long userId, String content) {
        return new MessageSendCommand(chatRoomId, userId, content);
    }
}