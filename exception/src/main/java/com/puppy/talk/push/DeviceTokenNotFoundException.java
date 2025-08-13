package com.puppy.talk.push;

import com.puppy.talk.user.UserIdentity;

/**
 * 디바이스 토큰을 찾을 수 없을 때 발생하는 예외
 */
public class DeviceTokenNotFoundException extends RuntimeException {
    
    public DeviceTokenNotFoundException(UserIdentity userId) {
        super("DeviceToken not found for user: " + userId.id());
    }
    
    public DeviceTokenNotFoundException(String token) {
        super("DeviceToken not found: " + token);
    }
}