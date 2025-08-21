package com.puppy.talk.chat.service;

import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.chat.Message;
import com.puppy.talk.chat.MessageIdentity;
import java.util.List;

/**
 * 메시지 관련 도메인 서비스
 * 
 * 메시지 처리와 관련된 순수한 비즈니스 로직을 담당합니다.
 * 인프라스트럭처 세부사항에 의존하지 않습니다.
 */
public interface MessageLookUpService {

    Message findMessage(MessageIdentity identity);

    List<Message> findMessagesByChatRoomId(ChatRoomIdentity chatRoomId);

    List<Message> findMessagesByChatRoomIdOrderByCreatedAtDesc(ChatRoomIdentity chatRoomId);

    List<Message> findUnreadMessagesByChatRoomId(ChatRoomIdentity chatRoomId);

    Message sendMessage(Message message);

    void markMessageAsRead(MessageIdentity identity);

    void markAllMessagesAsReadByChatRoomId(ChatRoomIdentity chatRoomId);

    void deleteMessage(MessageIdentity identity);
}