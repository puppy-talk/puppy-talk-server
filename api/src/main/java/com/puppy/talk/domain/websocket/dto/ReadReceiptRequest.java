package com.puppy.talk.domain.websocket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 읽음 확인 요청 DTO
 */
public record ReadReceiptRequest(
    @NotNull @Positive Long userId,
    Long lastReadMessageId
) {}
