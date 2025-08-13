package com.puppy.talk.chat;

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