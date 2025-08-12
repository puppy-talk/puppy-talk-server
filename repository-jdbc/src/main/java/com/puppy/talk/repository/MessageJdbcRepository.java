package com.puppy.talk.repository;

import com.puppy.talk.infrastructure.MessageRepository;
import com.puppy.talk.model.chat.ChatRoomIdentity;
import com.puppy.talk.model.chat.Message;
import com.puppy.talk.model.chat.MessageIdentity;
import com.puppy.talk.model.chat.SenderType;
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
public class MessageJdbcRepository implements MessageRepository {

    private static final RowMapper<Message> MESSAGE_ROW_MAPPER = (rs, rowNum) -> new Message(
        MessageIdentity.of(rs.getLong("id")),
        ChatRoomIdentity.of(rs.getLong("chat_room_id")),
        SenderType.valueOf(rs.getString("sender_type")),
        rs.getString("content"),
        rs.getBoolean("is_read"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Message> findByIdentity(MessageIdentity identity) {
        String sql = "SELECT id, chat_room_id, sender_type, content, is_read, created_at FROM messages WHERE id = ?";
        List<Message> messages = jdbcTemplate.query(sql, MESSAGE_ROW_MAPPER, identity.id());
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
    }

    @Override
    public List<Message> findByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = "SELECT id, chat_room_id, sender_type, content, is_read, created_at FROM messages WHERE chat_room_id = ?";
        return jdbcTemplate.query(sql, MESSAGE_ROW_MAPPER, chatRoomId.id());
    }

    @Override
    public List<Message> findByChatRoomIdOrderByCreatedAtDesc(ChatRoomIdentity chatRoomId) {
        String sql = "SELECT id, chat_room_id, sender_type, content, is_read, created_at FROM messages WHERE chat_room_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, MESSAGE_ROW_MAPPER, chatRoomId.id());
    }

    @Override
    public List<Message> findUnreadMessagesByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = "SELECT id, chat_room_id, sender_type, content, is_read, created_at FROM messages WHERE chat_room_id = ? AND is_read = false";
        return jdbcTemplate.query(sql, MESSAGE_ROW_MAPPER, chatRoomId.id());
    }

    @Override
    public Message save(Message message) {
        if (message.identity().id() == null) {
            return insert(message);
        } else {
            return update(message);
        }
    }

    @Override
    public void deleteByIdentity(MessageIdentity identity) {
        String sql = "DELETE FROM messages WHERE id = ?";
        jdbcTemplate.update(sql, identity.id());
    }

    @Override
    public void markAsRead(MessageIdentity identity) {
        String sql = "UPDATE messages SET is_read = true WHERE id = ?";
        jdbcTemplate.update(sql, identity.id());
    }

    @Override
    public void markAllAsReadByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = "UPDATE messages SET is_read = true WHERE chat_room_id = ?";
        jdbcTemplate.update(sql, chatRoomId.id());
    }

    private Message insert(Message message) {
        String sql = "INSERT INTO messages (chat_room_id, sender_type, content, is_read, created_at) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, message.chatRoomId().id());
            ps.setString(2, message.senderType().name());
            ps.setString(3, message.content());
            ps.setBoolean(4, message.isRead());
            ps.setTimestamp(5, Timestamp.valueOf(message.createdAt()));
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return new Message(
            MessageIdentity.of(generatedId),
            message.chatRoomId(),
            message.senderType(),
            message.content(),
            message.isRead(),
            message.createdAt()
        );
    }

    private Message update(Message message) {
        String sql = "UPDATE messages SET chat_room_id = ?, sender_type = ?, content = ?, is_read = ? WHERE id = ?";
        jdbcTemplate.update(sql, message.chatRoomId().id(), message.senderType().name(),
            message.content(), message.isRead(), message.identity().id());
        return message;
    }
}