package com.puppytalk.chat;

/**
 * 채팅방 상태 열거형
 */
public enum ChatRoomStatus {
    
    /**
     * 활성 상태 - 채팅 가능
     */
    ACTIVE("활성"),
    
    /**
     * 비활성 상태 - 일시적으로 채팅 불가능
     */
    INACTIVE("비활성"),
    
    /**
     * 삭제 상태 - 소프트 삭제됨
     */
    DELETED("삭제됨");
    
    private final String description;
    
    ChatRoomStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 채팅 가능한 상태인지 확인
     */
    public boolean isChatAvailable() {
        return this == ACTIVE;
    }
    
    /**
     * 활성 상태인지 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    /**
     * 삭제된 상태인지 확인
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}