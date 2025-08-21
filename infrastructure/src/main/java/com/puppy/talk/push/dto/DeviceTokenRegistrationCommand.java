package com.puppy.talk.push.dto;

import com.puppy.talk.user.UserIdentity;

public record DeviceTokenRegistrationCommand(
    UserIdentity userId,
    String token,
    String deviceId,
    String platform
) {
    public static DeviceTokenRegistrationCommand of(
        Long userId,
        String token,
        String deviceId,
        String platform
    ) {
        return new DeviceTokenRegistrationCommand(
            UserIdentity.of(userId),
            token,
            deviceId,
            platform
        );
    }
}