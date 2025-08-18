package com.puppy.talk.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
    String content
) {
}
