package com.puppy.talk.model.push;

/**
 * 디바이스 토큰 식별자
 */
public record DeviceTokenIdentity(Long id) {
    
    public DeviceTokenIdentity {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }
    
    public static DeviceTokenIdentity of(Long id) {
        return new DeviceTokenIdentity(id);
    }
}