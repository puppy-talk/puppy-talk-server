package com.puppy.talk.service.chat;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import java.util.List;

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