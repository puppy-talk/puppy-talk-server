package com.puppy.talk;

import com.puppy.talk.activity.InactivityNotificationRepository;
import com.puppy.talk.activity.InactivityNotification;
import com.puppy.talk.activity.InactivityNotificationIdentity;
import com.puppy.talk.activity.NotificationStatus;
import com.puppy.talk.chat.ChatRoomIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InactivityNotificationJdbcRepository implements InactivityNotificationRepository {

    private static final RowMapper<InactivityNotification> INACTIVITY_NOTIFICATION_ROW_MAPPER = (rs, rowNum) -> {
        Timestamp sentAtTimestamp = rs.getTimestamp("sent_at");
        LocalDateTime sentAt = sentAtTimestamp != null ? sentAtTimestamp.toLocalDateTime() : null;
        
        return new InactivityNotification(
            InactivityNotificationIdentity.of(rs.getLong("id")),
            ChatRoomIdentity.of(rs.getLong("chat_room_id")),
            rs.getTimestamp("last_activity_at").toLocalDateTime(),
            rs.getTimestamp("notification_eligible_at").toLocalDateTime(),
            NotificationStatus.valueOf(rs.getString("status")),
            rs.getString("ai_generated_message"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            sentAt
        );
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public InactivityNotification save(InactivityNotification notification) {
        if (notification.identity() == null) {
            return insert(notification);
        } else {
            return update(notification);
        }
    }

    private InactivityNotification insert(InactivityNotification notification) {
        String sql = """
            INSERT INTO inactivity_notifications 
            (chat_room_id, last_activity_at, notification_eligible_at, status, ai_generated_message, created_at, sent_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, notification.chatRoomId().id());
            ps.setTimestamp(2, Timestamp.valueOf(notification.lastActivityAt()));
            ps.setTimestamp(3, Timestamp.valueOf(notification.notificationEligibleAt()));
            ps.setString(4, notification.status().name());
            ps.setString(5, notification.aiGeneratedMessage());
            ps.setTimestamp(6, Timestamp.valueOf(notification.createdAt()));
            
            if (notification.sentAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(notification.sentAt()));
            } else {
                ps.setTimestamp(7, null);
            }
            
            return ps;
        }, keyHolder);
        
        Long generatedId = keyHolder.getKey().longValue();
        return notification.withIdentity(InactivityNotificationIdentity.of(generatedId));
    }

    private InactivityNotification update(InactivityNotification notification) {
        String sql = """
            UPDATE inactivity_notifications 
            SET chat_room_id = ?, last_activity_at = ?, notification_eligible_at = ?, 
                status = ?, ai_generated_message = ?, created_at = ?, sent_at = ?
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql,
            notification.chatRoomId().id(),
            Timestamp.valueOf(notification.lastActivityAt()),
            Timestamp.valueOf(notification.notificationEligibleAt()),
            notification.status().name(),
            notification.aiGeneratedMessage(),
            Timestamp.valueOf(notification.createdAt()),
            notification.sentAt() != null ? Timestamp.valueOf(notification.sentAt()) : null,
            notification.identity().id()
        );
        
        return notification;
    }

    @Override
    public Optional<InactivityNotification> findByIdentity(InactivityNotificationIdentity identity) {
        String sql = """
            SELECT id, chat_room_id, last_activity_at, notification_eligible_at, 
                   status, ai_generated_message, created_at, sent_at 
            FROM inactivity_notifications 
            WHERE id = ?
            """;
        
        List<InactivityNotification> notifications = jdbcTemplate.query(sql, 
            INACTIVITY_NOTIFICATION_ROW_MAPPER, identity.id());
        return notifications.isEmpty() ? Optional.empty() : Optional.of(notifications.get(0));
    }

    @Override
    public Optional<InactivityNotification> findByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = """
            SELECT id, chat_room_id, last_activity_at, notification_eligible_at, 
                   status, ai_generated_message, created_at, sent_at 
            FROM inactivity_notifications 
            WHERE chat_room_id = ?
            """;
        
        List<InactivityNotification> notifications = jdbcTemplate.query(sql, 
            INACTIVITY_NOTIFICATION_ROW_MAPPER, chatRoomId.id());
        return notifications.isEmpty() ? Optional.empty() : Optional.of(notifications.get(0));
    }

    @Override
    public List<InactivityNotification> findByStatus(NotificationStatus status) {
        String sql = """
            SELECT id, chat_room_id, last_activity_at, notification_eligible_at, 
                   status, ai_generated_message, created_at, sent_at 
            FROM inactivity_notifications 
            WHERE status = ?
            ORDER BY notification_eligible_at ASC
            """;
        
        return jdbcTemplate.query(sql, INACTIVITY_NOTIFICATION_ROW_MAPPER, status.name());
    }

    @Override
    public List<InactivityNotification> findEligibleNotifications() {
        String sql = """
            SELECT id, chat_room_id, last_activity_at, notification_eligible_at, 
                   status, ai_generated_message, created_at, sent_at 
            FROM inactivity_notifications 
            WHERE status = 'PENDING' 
              AND notification_eligible_at <= ? 
            ORDER BY notification_eligible_at ASC
            """;
        
        return jdbcTemplate.query(sql, INACTIVITY_NOTIFICATION_ROW_MAPPER, 
            Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public List<InactivityNotification> findByCreatedAtBefore(LocalDateTime beforeTime) {
        String sql = """
            SELECT id, chat_room_id, last_activity_at, notification_eligible_at, 
                   status, ai_generated_message, created_at, sent_at 
            FROM inactivity_notifications 
            WHERE created_at < ?
            ORDER BY created_at DESC
            """;
        
        return jdbcTemplate.query(sql, INACTIVITY_NOTIFICATION_ROW_MAPPER, 
            Timestamp.valueOf(beforeTime));
    }

    @Override
    public void deleteByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = "DELETE FROM inactivity_notifications WHERE chat_room_id = ?";
        jdbcTemplate.update(sql, chatRoomId.id());
    }

    @Override
    public void delete(InactivityNotification notification) {
        String sql = "DELETE FROM inactivity_notifications WHERE id = ?";
        jdbcTemplate.update(sql, notification.identity().id());
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM inactivity_notifications";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public long countByStatus(NotificationStatus status) {
        String sql = "SELECT COUNT(*) FROM inactivity_notifications WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, status.name());
    }
}