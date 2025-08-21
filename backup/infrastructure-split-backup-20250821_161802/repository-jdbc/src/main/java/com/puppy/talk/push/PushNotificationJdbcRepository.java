package com.puppy.talk.push;

import com.puppy.talk.push.PushNotificationRepository;
import com.puppy.talk.push.NotificationType;
import com.puppy.talk.push.PushNotification;
import com.puppy.talk.push.PushNotificationIdentity;
import com.puppy.talk.push.PushNotificationStatus;
import com.puppy.talk.user.UserIdentity;
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

/**
 * 푸시 알림 JDBC Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class PushNotificationJdbcRepository implements PushNotificationRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    private static final RowMapper<PushNotification> PUSH_NOTIFICATION_ROW_MAPPER = (rs, rowNum) -> {
        Timestamp scheduledTimestamp = rs.getTimestamp("scheduled_at");
        Timestamp sentTimestamp = rs.getTimestamp("sent_at");
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        
        return new PushNotification(
            PushNotificationIdentity.of(rs.getLong("id")),
            UserIdentity.of(rs.getLong("user_id")),
            rs.getString("device_token"),
            NotificationType.valueOf(rs.getString("notification_type")),
            rs.getString("title"),
            rs.getString("message"),
            rs.getString("data"),
            PushNotificationStatus.valueOf(rs.getString("status")),
            rs.getString("error_message"),
            scheduledTimestamp != null ? scheduledTimestamp.toLocalDateTime() : null,
            sentTimestamp != null ? sentTimestamp.toLocalDateTime() : null,
            createdTimestamp.toLocalDateTime()
        );
    };
    
    @Override
    public PushNotification save(PushNotification notification) {
        if (notification.identity() == null) {
            return insert(notification);
        } else {
            return update(notification);
        }
    }
    
    private PushNotification insert(PushNotification notification) {
        String sql = """
            INSERT INTO push_notifications 
            (user_id, device_token, notification_type, title, message, data, status, error_message, scheduled_at, sent_at, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, notification.userId().id());
            ps.setString(2, notification.deviceToken());
            ps.setString(3, notification.notificationType().name());
            ps.setString(4, notification.title());
            ps.setString(5, notification.message());
            ps.setString(6, notification.data());
            ps.setString(7, notification.status().name());
            ps.setString(8, notification.errorMessage());
            ps.setTimestamp(9, notification.scheduledAt() != null ? Timestamp.valueOf(notification.scheduledAt()) : null);
            ps.setTimestamp(10, notification.sentAt() != null ? Timestamp.valueOf(notification.sentAt()) : null);
            ps.setTimestamp(11, Timestamp.valueOf(notification.createdAt()));
            return ps;
        }, keyHolder);
        
        Long generatedId = keyHolder.getKey().longValue();
        return notification.withIdentity(PushNotificationIdentity.of(generatedId));
    }
    
    private PushNotification update(PushNotification notification) {
        String sql = """
            UPDATE push_notifications 
            SET device_token = ?, notification_type = ?, title = ?, message = ?, data = ?, 
                status = ?, error_message = ?, scheduled_at = ?, sent_at = ?
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql,
            notification.deviceToken(),
            notification.notificationType().name(),
            notification.title(),
            notification.message(),
            notification.data(),
            notification.status().name(),
            notification.errorMessage(),
            notification.scheduledAt() != null ? Timestamp.valueOf(notification.scheduledAt()) : null,
            notification.sentAt() != null ? Timestamp.valueOf(notification.sentAt()) : null,
            notification.identity().id()
        );
        
        return notification;
    }
    
    @Override
    public Optional<PushNotification> findByIdentity(PushNotificationIdentity identity) {
        String sql = "SELECT * FROM push_notifications WHERE id = ?";
        
        List<PushNotification> results = jdbcTemplate.query(sql, PUSH_NOTIFICATION_ROW_MAPPER, identity.id());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<PushNotification> findByUserId(UserIdentity userId) {
        String sql = "SELECT * FROM push_notifications WHERE user_id = ? ORDER BY created_at DESC";
        
        return jdbcTemplate.query(sql, PUSH_NOTIFICATION_ROW_MAPPER, userId.id());
    }
    
    @Override
    public List<PushNotification> findPendingNotifications() {
        String sql = """
            SELECT * FROM push_notifications 
            WHERE status = 'PENDING' AND scheduled_at <= ?
            ORDER BY scheduled_at ASC
            """;
        
        return jdbcTemplate.query(sql, PUSH_NOTIFICATION_ROW_MAPPER, Timestamp.valueOf(LocalDateTime.now()));
    }
    
    @Override
    public List<PushNotification> findPendingNotificationsBefore(LocalDateTime before) {
        String sql = """
            SELECT * FROM push_notifications 
            WHERE status = 'PENDING' AND scheduled_at <= ?
            ORDER BY scheduled_at ASC
            """;
        
        return jdbcTemplate.query(sql, PUSH_NOTIFICATION_ROW_MAPPER, Timestamp.valueOf(before));
    }
    
    @Override
    public long countByStatus(PushNotificationStatus status) {
        String sql = "SELECT COUNT(*) FROM push_notifications WHERE status = ?";
        
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status.name());
        return count != null ? count : 0L;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM push_notifications";
        
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
    
    @Override
    public List<PushNotification> findRecentByUserId(UserIdentity userId, int limit) {
        String sql = """
            SELECT * FROM push_notifications 
            WHERE user_id = ? 
            ORDER BY created_at DESC 
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql, PUSH_NOTIFICATION_ROW_MAPPER, userId.id(), limit);
    }
}