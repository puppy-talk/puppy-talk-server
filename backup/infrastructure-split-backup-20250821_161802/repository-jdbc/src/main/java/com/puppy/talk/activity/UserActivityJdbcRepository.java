package com.puppy.talk.activity;

import com.puppy.talk.activity.UserActivityRepository;
import com.puppy.talk.activity.ActivityType;
import com.puppy.talk.activity.UserActivity;
import com.puppy.talk.activity.UserActivityIdentity;
import com.puppy.talk.chat.ChatRoomIdentity;
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

@Repository
@RequiredArgsConstructor
public class UserActivityJdbcRepository implements UserActivityRepository {

    private static final RowMapper<UserActivity> USER_ACTIVITY_ROW_MAPPER = (rs, rowNum) -> new UserActivity(
        UserActivityIdentity.of(rs.getLong("id")),
        UserIdentity.of(rs.getLong("user_id")),
        ChatRoomIdentity.of(rs.getLong("chat_room_id")),
        ActivityType.valueOf(rs.getString("activity_type")),
        rs.getTimestamp("activity_at").toLocalDateTime(),
        rs.getTimestamp("created_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserActivity save(UserActivity userActivity) {
        if (userActivity.identity() == null) {
            return insert(userActivity);
        } else {
            return update(userActivity);
        }
    }

    private UserActivity insert(UserActivity userActivity) {
        String sql = """
            INSERT INTO user_activities (user_id, chat_room_id, activity_type, activity_at, created_at) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userActivity.userId().id());
            ps.setLong(2, userActivity.chatRoomId().id());
            ps.setString(3, userActivity.activityType().name());
            ps.setTimestamp(4, Timestamp.valueOf(userActivity.activityAt()));
            ps.setTimestamp(5, Timestamp.valueOf(userActivity.createdAt()));
            return ps;
        }, keyHolder);
        
        Long generatedId = keyHolder.getKey().longValue();
        return userActivity.withIdentity(UserActivityIdentity.of(generatedId));
    }

    private UserActivity update(UserActivity userActivity) {
        String sql = """
            UPDATE user_activities 
            SET user_id = ?, chat_room_id = ?, activity_type = ?, activity_at = ?, created_at = ?
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql,
            userActivity.userId().id(),
            userActivity.chatRoomId().id(),
            userActivity.activityType().name(),
            Timestamp.valueOf(userActivity.activityAt()),
            Timestamp.valueOf(userActivity.createdAt()),
            userActivity.identity().id()
        );
        
        return userActivity;
    }

    @Override
    public Optional<UserActivity> findByIdentity(UserActivityIdentity identity) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE id = ?
            """;
        
        List<UserActivity> activities = jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, identity.id());
        return activities.isEmpty() ? Optional.empty() : Optional.of(activities.get(0));
    }

    @Override
    public List<UserActivity> findByUserId(UserIdentity userId) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE user_id = ? 
            ORDER BY activity_at DESC
            """;
        
        return jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, userId.id());
    }

    @Override
    public List<UserActivity> findByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE chat_room_id = ? 
            ORDER BY activity_at DESC
            """;
        
        return jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, chatRoomId.id());
    }

    @Override
    public Optional<UserActivity> findLastActivityByUserId(UserIdentity userId) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE user_id = ? 
            ORDER BY activity_at DESC 
            LIMIT 1
            """;
        
        List<UserActivity> activities = jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, userId.id());
        return activities.isEmpty() ? Optional.empty() : Optional.of(activities.get(0));
    }

    @Override
    public Optional<UserActivity> findLastActivityByChatRoomId(ChatRoomIdentity chatRoomId) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE chat_room_id = ? 
            ORDER BY activity_at DESC 
            LIMIT 1
            """;
        
        List<UserActivity> activities = jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, chatRoomId.id());
        return activities.isEmpty() ? Optional.empty() : Optional.of(activities.get(0));
    }

    @Override
    public List<ChatRoomIdentity> findChatRoomsWithLastActivityBefore(LocalDateTime beforeTime) {
        String sql = """
            SELECT DISTINCT chat_room_id 
            FROM user_activities ua1
            WHERE activity_at = (
                SELECT MAX(activity_at) 
                FROM user_activities ua2 
                WHERE ua2.chat_room_id = ua1.chat_room_id
            )
            AND activity_at < ?
            """;
        
        return jdbcTemplate.query(sql, 
            (rs, rowNum) -> ChatRoomIdentity.of(rs.getLong("chat_room_id")),
            Timestamp.valueOf(beforeTime)
        );
    }

    @Override
    public List<UserActivity> findByActivityType(ActivityType activityType) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE activity_type = ? 
            ORDER BY activity_at DESC
            """;
        
        return jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, activityType.name());
    }

    @Override
    public List<UserActivity> findByActivityAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = """
            SELECT id, user_id, chat_room_id, activity_type, activity_at, created_at 
            FROM user_activities 
            WHERE activity_at BETWEEN ? AND ?
            ORDER BY activity_at DESC
            """;
        
        return jdbcTemplate.query(sql, USER_ACTIVITY_ROW_MAPPER, 
            Timestamp.valueOf(startTime), 
            Timestamp.valueOf(endTime)
        );
    }
}