package com.puppy.talk;

import com.puppy.talk.chat.ChatRoomRepository;
import com.puppy.talk.chat.ChatRoom;
import com.puppy.talk.chat.ChatRoomIdentity;
import com.puppy.talk.pet.PetIdentity;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRoomJdbcRepository implements ChatRoomRepository {

    private static final RowMapper<ChatRoom> CHAT_ROOM_ROW_MAPPER = (rs, rowNum) -> {
        Timestamp lastMessageTimestamp = rs.getTimestamp("last_message_at");
        return new ChatRoom(
            ChatRoomIdentity.of(rs.getLong("id")),
            PetIdentity.of(rs.getLong("pet_id")),
            rs.getString("room_name"),
            lastMessageTimestamp != null ? lastMessageTimestamp.toLocalDateTime() : null
        );
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ChatRoom> findByIdentity(ChatRoomIdentity identity) {
        String sql = "SELECT id, pet_id, room_name, last_message_at FROM chat_rooms WHERE id = ?";
        List<ChatRoom> chatRooms = jdbcTemplate.query(sql, CHAT_ROOM_ROW_MAPPER, identity.id());
        return chatRooms.isEmpty() ? Optional.empty() : Optional.of(chatRooms.get(0));
    }

    @Override
    public Optional<ChatRoom> findByPetId(PetIdentity petId) {
        String sql = "SELECT id, pet_id, room_name, last_message_at FROM chat_rooms WHERE pet_id = ?";
        List<ChatRoom> chatRooms = jdbcTemplate.query(sql, CHAT_ROOM_ROW_MAPPER, petId.id());
        return chatRooms.isEmpty() ? Optional.empty() : Optional.of(chatRooms.get(0));
    }

    @Override
    public List<ChatRoom> findAll() {
        String sql = "SELECT id, pet_id, room_name, last_message_at FROM chat_rooms";
        return jdbcTemplate.query(sql, CHAT_ROOM_ROW_MAPPER);
    }

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        if (chatRoom.identity().id() == null) {
            return insert(chatRoom);
        } else {
            return update(chatRoom);
        }
    }

    @Override
    public void deleteByIdentity(ChatRoomIdentity identity) {
        String sql = "DELETE FROM chat_rooms WHERE id = ?";
        jdbcTemplate.update(sql, identity.id());
    }

    private ChatRoom insert(ChatRoom chatRoom) {
        String sql = "INSERT INTO chat_rooms (pet_id, room_name, last_message_at) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, chatRoom.petId().id());
            ps.setString(2, chatRoom.roomName());
            ps.setTimestamp(3, chatRoom.lastMessageAt() != null ?
                Timestamp.valueOf(chatRoom.lastMessageAt()) : null);
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return new ChatRoom(
            ChatRoomIdentity.of(generatedId),
            chatRoom.petId(),
            chatRoom.roomName(),
            chatRoom.lastMessageAt()
        );
    }

    private ChatRoom update(ChatRoom chatRoom) {
        String sql = "UPDATE chat_rooms SET pet_id = ?, room_name = ?, last_message_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, chatRoom.petId().id(), chatRoom.roomName(),
            chatRoom.lastMessageAt() != null ? Timestamp.valueOf(chatRoom.lastMessageAt())
                : null,
            chatRoom.identity().id());
        return chatRoom;
    }
}