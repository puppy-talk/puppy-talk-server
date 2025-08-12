package com.puppy.talk.repository;

import com.puppy.talk.infrastructure.user.UserRepository;
import com.puppy.talk.model.user.User;
import com.puppy.talk.model.user.UserIdentity;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
public class UserJdbcRepository implements UserRepository {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
        UserIdentity.of(rs.getLong("id")),
        rs.getString("username"),
        rs.getString("email"),
        rs.getString("password")
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<User> findByIdentity(UserIdentity identity) {
        String sql = "SELECT id, username, email, password FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, identity.id());
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, email, password FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, username, email, password FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, email, password FROM users";
        return jdbcTemplate.query(sql, USER_ROW_MAPPER);
    }

    @Override
    public User save(User user) {
        if (user.identity().id() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    @Override
    public void deleteByIdentity(UserIdentity identity) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, identity.id());
    }

    private User insert(User user) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.username());
            ps.setString(2, user.email());
            ps.setString(3, user.password());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        return new User(
            UserIdentity.of(generatedId),
            user.username(),
            user.email(),
            user.password()
        );
    }

    private User update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.username(), user.email(), user.password(),
            user.identity().id());
        return user;
    }
}