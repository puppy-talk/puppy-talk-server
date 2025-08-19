package com.puppy.talk.facade;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;

import java.time.LocalDateTime;

/**
 * 페르소나 정보가 포함된 채팅방 정보
 * 
 * ChatFacade에서 사용되는 결과 객체로, 채팅방 기본 정보에
 * 펫 정보, 페르소나 정보, 읽지 않은 메시지 수, 마지막 활동 시간을 포함합니다.
 */
public record EnrichedChatRoomInfo(
    ChatRoom chatRoom,
    Pet pet,
    Persona persona,
    int unreadMessageCount,
    LocalDateTime lastActivity
) {
    
    /**
     * 기본 생성자에서 null 검증과 값 검증
     */
    public EnrichedChatRoomInfo {
        if (chatRoom == null) {
            throw new IllegalArgumentException("ChatRoom cannot be null");
        }
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        if (unreadMessageCount < 0) {
            throw new IllegalArgumentException("Unread message count cannot be negative");
        }
        if (lastActivity == null) {
            throw new IllegalArgumentException("Last activity cannot be null");
        }
    }
    
    /**
     * 편의 메서드: 채팅방 ID 반환
     */
    public Long getChatRoomId() {
        return chatRoom.identity().id();
    }
    
    /**
     * 편의 메서드: 펫 이름 반환
     */
    public String getPetName() {
        return pet.name();
    }
    
    /**
     * 편의 메서드: 펫 품종 반환
     */
    public String getPetBreed() {
        return pet.breed();
    }
    
    /**
     * 편의 메서드: 페르소나 특성 반환
     */
    public String getPersonalityTraits() {
        return persona.personalityTraits();
    }
    
    /**
     * 편의 메서드: 읽지 않은 메시지가 있는지 확인
     */
    public boolean hasUnreadMessages() {
        return unreadMessageCount > 0;
    }
    
    /**
     * 편의 메서드: 마지막 활동으로부터 경과 시간(분) 계산
     */
    public long getMinutesSinceLastActivity() {
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).toMinutes();
    }
    
    /**
     * 편의 메서드: 최근 활동 여부 확인 (30분 이내)
     */
    public boolean isRecentlyActive() {
        return getMinutesSinceLastActivity() <= 30;
    }
}