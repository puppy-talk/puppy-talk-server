package com.puppy.talk.infrastructure.chat;

import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {

    Optional<Message> findByIdentity(MessageIdentity identity);

    List<Message> findByChatRoomId(ChatRoomIdentity chatRoomId);

    List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomIdentity chatRoomId);

    List<Message> findUnreadMessagesByChatRoomId(ChatRoomIdentity chatRoomId);

    Message save(Message message);

    void deleteByIdentity(MessageIdentity identity);

    void markAsRead(MessageIdentity identity);

    void markAllAsReadByChatRoomId(ChatRoomIdentity chatRoomId);
}