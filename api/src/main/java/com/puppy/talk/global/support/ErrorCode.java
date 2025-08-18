package com.puppy.talk.global.support;

public enum ErrorCode {
    // User related errors
    USER_NOT_FOUND("USER_001"),
    DUPLICATE_USERNAME("USER_002"),
    DUPLICATE_EMAIL("USER_003"),
    
    // Pet related errors
    PET_NOT_FOUND("PET_001"),
    PERSONA_NOT_FOUND("PET_002"),
    
    // Chat related errors
    CHAT_ROOM_NOT_FOUND("CHAT_001"),
    MESSAGE_NOT_FOUND("CHAT_002"),
    
    // Validation errors
    VALIDATION_ERROR("VAL_001"),
    
    // Server errors
    INTERNAL_SERVER_ERROR("SRV_001");
    
    private final String code;
    
    ErrorCode(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return code;
    }
}

