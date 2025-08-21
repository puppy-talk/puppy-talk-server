package com.puppy.talk.dto;

import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.Message;
import com.puppy.talk.pet.Pet;
import com.puppy.talk.pet.Persona;

import java.util.List;

/**
 * 페르소나 정보가 포함된 채팅 시작 결과
 * 
 * ChatFacade에서 사용되는 결과 객체로, 기본 채팅 시작 정보에
 * 페르소나 정보를 추가하여 더 풍부한 컨텍스트를 제공합니다.
 */
public record EnrichedChatStartResult(
    ChatRoom chatRoom,
    Pet pet,
    Persona persona,
    List<Message> recentMessages
) {
    
    /**
     * 기본 생성자에서 null 검증
     */
    public EnrichedChatStartResult {
        if (chatRoom == null) {
            throw new IllegalArgumentException("ChatRoom cannot be null");
        }
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }
        if (persona == null) {
            throw new IllegalArgumentException("Persona cannot be null");
        }
        if (recentMessages == null) {
            throw new IllegalArgumentException("Recent messages cannot be null");
        }
    }
    
    /**
     * 편의 메서드: 최근 메시지 개수 반환
     */
    public int getRecentMessageCount() {
        return recentMessages.size();
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
     * 편의 메서드: 페르소나 타입 반환
     */
    public String getPersonaType() {
        return persona.personalityTraits();
    }
}