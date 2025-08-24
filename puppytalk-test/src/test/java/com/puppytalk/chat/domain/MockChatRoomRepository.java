package com.puppytalk.chat.domain;

import com.puppytalk.chat.ChatRoom;
import com.puppytalk.chat.ChatRoomId;
import com.puppytalk.chat.ChatRoomRepository;
import com.puppytalk.pet.PetId;
import com.puppytalk.user.UserId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 ChatRoomRepository Mock 구현체
 */
public class MockChatRoomRepository implements ChatRoomRepository {

    private final Map<ChatRoomId, ChatRoom> chatRooms = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        if (chatRoom.id() == null) {
            // 새로운 ID 생성
            ChatRoomId newId = ChatRoomId.of(idGenerator.getAndIncrement());
            ChatRoom savedChatRoom = ChatRoom.of(
                newId,
                chatRoom.userId(),
                chatRoom.petId(),
                chatRoom.createdAt(),
                chatRoom.lastMessageAt()
            );
            chatRooms.put(newId, savedChatRoom);
            return savedChatRoom;
        } else {
            // 기존 채팅방 업데이트
            chatRooms.put(chatRoom.id(), chatRoom);
            return chatRoom;
        }
    }

    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        return Optional.ofNullable(chatRooms.get(id));
    }

    @Override
    public Optional<ChatRoom> findByUserIdAndPetId(UserId userId, PetId petId) {
        return chatRooms.values().stream()
            .filter(chatRoom -> chatRoom.userId().equals(userId) && chatRoom.petId().equals(petId))
            .findFirst();
    }

    @Override
    public List<ChatRoom> findByUserId(UserId userId) {
        return chatRooms.values().stream()
            .filter(chatRoom -> chatRoom.userId().equals(userId))
            .toList();
    }

    @Override
    public boolean existsById(ChatRoomId id) {
        return chatRooms.containsKey(id);
    }

    @Override
    public void deleteById(ChatRoomId id) {
        chatRooms.remove(id);
    }

    @Override
    public long count() {
        return chatRooms.size();
    }

    // 테스트용 헬퍼 메서드
    public void addChatRoom(ChatRoom chatRoom) {
        if (chatRoom.id() != null) {
            chatRooms.put(chatRoom.id(), chatRoom);
        }
    }

    public void clear() {
        chatRooms.clear();
        idGenerator.set(1);
    }

    public Map<ChatRoomId, ChatRoom> getAllChatRooms() {
        return new HashMap<>(chatRooms);
    }
}