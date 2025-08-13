package com.puppy.talk;

import com.puppy.talk.push.DeviceTokenRepository;
import com.puppy.talk.push.DeviceToken;
import com.puppy.talk.push.DeviceTokenIdentity;
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
import java.util.List;
import java.util.Optional;

/**
 * 디바이스 토큰 JDBC Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class DeviceTokenJdbcRepository implements DeviceTokenRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    private static final RowMapper<DeviceToken> DEVICE_TOKEN_ROW_MAPPER = (rs, rowNum) -> DeviceToken.of(
        UserIdentity.of(rs.getLong("user_id")),
        rs.getString("token"),
        rs.getString("device_id"),
        rs.getString("platform")
    ).withIdentity(DeviceTokenIdentity.of(rs.getLong("id")))
     .updateLastUsed() // 마지막 사용 시간은 조회된 시간으로 설정
     .activate(); // 기본적으로 활성 상태로 설정
    
    private static final RowMapper<DeviceToken> FULL_DEVICE_TOKEN_ROW_MAPPER = (rs, rowNum) -> {
        DeviceToken token = DeviceToken.of(
            UserIdentity.of(rs.getLong("user_id")),
            rs.getString("token"),
            rs.getString("device_id"),
            rs.getString("platform")
        ).withIdentity(DeviceTokenIdentity.of(rs.getLong("id")));
        
        // 실제 데이터베이스 값들로 정확히 설정
        Timestamp lastUsedTimestamp = rs.getTimestamp("last_used_at");
        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        
        return new DeviceToken(
            token.identity(),
            token.userId(),
            token.token(),
            token.deviceId(),
            token.platform(),
            rs.getBoolean("is_active"),
            lastUsedTimestamp != null ? lastUsedTimestamp.toLocalDateTime() : token.lastUsedAt(),
            createdTimestamp != null ? createdTimestamp.toLocalDateTime() : token.createdAt()
        );
    };
    
    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        if (deviceToken.identity() == null) {
            return insert(deviceToken);
        } else {
            return update(deviceToken);
        }
    }
    
    private DeviceToken insert(DeviceToken deviceToken) {
        String sql = """
            INSERT INTO device_tokens (user_id, token, device_id, platform, is_active, last_used_at, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, deviceToken.userId().id());
            ps.setString(2, deviceToken.token());
            ps.setString(3, deviceToken.deviceId());
            ps.setString(4, deviceToken.platform());
            ps.setBoolean(5, deviceToken.isActive());
            ps.setTimestamp(6, Timestamp.valueOf(deviceToken.lastUsedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(deviceToken.createdAt()));
            return ps;
        }, keyHolder);
        
        Long generatedId = keyHolder.getKey().longValue();
        return deviceToken.withIdentity(DeviceTokenIdentity.of(generatedId));
    }
    
    private DeviceToken update(DeviceToken deviceToken) {
        String sql = """
            UPDATE device_tokens 
            SET token = ?, device_id = ?, platform = ?, is_active = ?, last_used_at = ?
            WHERE id = ?
            """;
        
        jdbcTemplate.update(sql,
            deviceToken.token(),
            deviceToken.deviceId(),
            deviceToken.platform(),
            deviceToken.isActive(),
            Timestamp.valueOf(deviceToken.lastUsedAt()),
            deviceToken.identity().id()
        );
        
        return deviceToken;
    }
    
    @Override
    public Optional<DeviceToken> findByIdentity(DeviceTokenIdentity identity) {
        String sql = "SELECT * FROM device_tokens WHERE id = ?";
        
        List<DeviceToken> results = jdbcTemplate.query(sql, FULL_DEVICE_TOKEN_ROW_MAPPER, identity.id());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public Optional<DeviceToken> findByToken(String token) {
        String sql = "SELECT * FROM device_tokens WHERE token = ?";
        
        List<DeviceToken> results = jdbcTemplate.query(sql, FULL_DEVICE_TOKEN_ROW_MAPPER, token);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<DeviceToken> findActiveByUserId(UserIdentity userId) {
        String sql = "SELECT * FROM device_tokens WHERE user_id = ? AND is_active = true ORDER BY last_used_at DESC";
        
        return jdbcTemplate.query(sql, FULL_DEVICE_TOKEN_ROW_MAPPER, userId.id());
    }
    
    @Override
    public List<DeviceToken> findByUserId(UserIdentity userId) {
        String sql = "SELECT * FROM device_tokens WHERE user_id = ? ORDER BY created_at DESC";
        
        return jdbcTemplate.query(sql, FULL_DEVICE_TOKEN_ROW_MAPPER, userId.id());
    }
    
    @Override
    public Optional<DeviceToken> findByUserIdAndDeviceId(UserIdentity userId, String deviceId) {
        if (deviceId == null) {
            return Optional.empty();
        }
        
        String sql = "SELECT * FROM device_tokens WHERE user_id = ? AND device_id = ?";
        
        List<DeviceToken> results = jdbcTemplate.query(sql, FULL_DEVICE_TOKEN_ROW_MAPPER, userId.id(), deviceId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM device_tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }
    
    @Override
    public void deactivateAllByUserId(UserIdentity userId) {
        String sql = "UPDATE device_tokens SET is_active = false WHERE user_id = ?";
        jdbcTemplate.update(sql, userId.id());
    }
    
    @Override
    public List<DeviceToken> findActiveByPlatform(String platform) {
        String sql = "SELECT * FROM device_tokens WHERE platform = ? AND is_active = true ORDER BY last_used_at DESC";
        
        return jdbcTemplate.query(sql, FULL_DEVICE_TOKEN_ROW_MAPPER, platform);
    }
}