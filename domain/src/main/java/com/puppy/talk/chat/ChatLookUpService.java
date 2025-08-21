package com.puppy.talk.chat;

import com.puppy.talk.chat.dto.ChatStartResult;
import com.puppy.talk.chat.dto.MessageSendCommand;
import com.puppy.talk.chat.dto.MessageSendResult;
import com.puppy.talk.pet.PetIdentity;
import com.puppy.talk.user.UserIdentity;

import java.util.List;

/**
 * 채팅 조회 서비스 인터페이스
 */
public interface ChatLookUpService {
    
    /**
     * 채팅을 시작합니다.
     * 
     * @param userId 사용자 식별자
     * @param petId 펫 식별자
     * @return 채팅 시작 결과
     */
    ChatStartResult startChat(UserIdentity userId, PetIdentity petId);
    
    /**
     * 메시지를 전송합니다.
     * 
     * @param command 메시지 전송 명령
     * @return 메시지 전송 결과
     */
    MessageSendResult sendMessage(MessageSendCommand command);
    
    /**
     * 사용자의 채팅방 목록을 조회합니다.
     * 
     * @param userId 사용자 식별자
     * @return 채팅방 목록
     */
    List<ChatRoom> getChatRooms(UserIdentity userId);
    
    /**
     * 채팅방의 메시지 히스토리를 조회합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param limit 조회 제한 수
     * @return 메시지 목록
     */
    List<Message> getMessageHistory(ChatRoomIdentity chatRoomId, int limit);
    
    /**
     * 채팅방의 읽지 않은 메시지를 모두 읽음 처리합니다.
     * 
     * @param chatRoomId 채팅방 식별자
     * @param userId 사용자 식별자
     */
    void markAllMessagesAsRead(ChatRoomIdentity chatRoomId, UserIdentity userId);
}