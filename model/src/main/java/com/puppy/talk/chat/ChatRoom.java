package com.puppy.talk.chat;

import com.puppy.talk.pet.PetIdentity;
import java.time.LocalDateTime;

public record ChatRoom(
    ChatRoomIdentity identity,
    PetIdentity petId,
    String roomName,
    LocalDateTime lastMessageAt
) {

    public ChatRoom {
        // identity can be null for new chat rooms before saving
        if (petId == null) {
            throw new IllegalArgumentException("PetId cannot be null");
        }
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        roomName = roomName.trim();
    }
    
    /**
     * 새로운 채팅방을 생성합니다.
     */
    public static ChatRoom of(
        ChatRoomIdentity identity,
        PetIdentity petId,
        String roomName,
        LocalDateTime lastMessageAt
    ) {
        return new ChatRoom(identity, petId, roomName, lastMessageAt);
    }
    
    /**
     * 마지막 메시지 시간을 지정하여 새로운 채팅방을 생성합니다.
     */
    public static ChatRoom of(
        PetIdentity petId,
        String roomName,
        LocalDateTime lastMessageAt
    ) {
        return new ChatRoom(null, petId, roomName, lastMessageAt);
    }
    
    /**
     * 현재 시각을 마지막 메시지 시간으로 하여 새로운 채팅방을 생성합니다.
     */
    public static ChatRoom of(
        PetIdentity petId,
        String roomName
    ) {
        return new ChatRoom(null, petId, roomName, LocalDateTime.now());
    }

    /**
     * 마지막 메시지 시간을 업데이트합니다.
     * 
     * @param messageTime 새로운 메시지 시간
     * @return 업데이트된 채팅방
     */
    public ChatRoom updateLastMessageTime(LocalDateTime messageTime) {
        return new ChatRoom(identity, petId, roomName, messageTime);
    }

    /**
     * 마지막 메시지 시간을 현재 시간으로 업데이트합니다.
     * 
     * @return 업데이트된 채팅방
     */
    public ChatRoom updateLastMessageTimeToNow() {
        return updateLastMessageTime(LocalDateTime.now());
    }

    /**
     * 채팅방이 특정 펫에 속하는지 확인합니다.
     * 
     * @param petId 확인할 펫 ID
     * @return 펫이 맞으면 true, 아니면 false
     */
    public boolean belongsToPet(PetIdentity petId) {
        return this.petId.equals(petId);
    }

    /**
     * 채팅방이 비활성 상태인지 확인합니다.
     * 
     * @param inactivityThresholdHours 비활성 임계값 (시간)
     * @return 비활성 상태이면 true, 아니면 false
     */
    public boolean isInactive(int inactivityThresholdHours) {
        if (lastMessageAt == null) {
            return false; // 새로운 채팅방은 비활성 상태가 아님
        }
        return lastMessageAt.isBefore(LocalDateTime.now().minusHours(inactivityThresholdHours));
    }

    /**
     * 채팅방이 최근에 활성화되었는지 확인합니다.
     * 
     * @param recentThresholdMinutes 최근 활동 임계값 (분)
     * @return 최근 활성화되었으면 true, 아니면 false
     */
    public boolean isRecentlyActive(int recentThresholdMinutes) {
        if (lastMessageAt == null) {
            return true; // 새로운 채팅방은 활성 상태
        }
        return lastMessageAt.isAfter(LocalDateTime.now().minusMinutes(recentThresholdMinutes));
    }
}