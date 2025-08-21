package com.puppy.talk.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 메시지 전송 명령
 * <p>
 * Bean Validation을 사용하여 입력 검증을 수행하며, 메시지 내용에 대한 비즈니스 규칙을 강제합니다.
 */
public record MessageSendCommand(

    @NotBlank(message = "메시지 내용은 비어있을 수 없습니다")
    @Size(max = 2000, message = "메시지 내용은 2000자를 초과할 수 없습니다")
    String content

) {

    public MessageSendCommand {
        if (content != null) {
            content = content.trim();
        }
    }

    public static MessageSendCommand of(String content) {
        return new MessageSendCommand(content);
    }
}
