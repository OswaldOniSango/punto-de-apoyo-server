package com.puntodeapoyo.users.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InternalUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public InternalUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<InternalUser> findByEmail(String email) {
        String sql = """
                SELECT id, first_name, last_name, email, phone, password_hash, role, status, created_at, updated_at
                FROM users
                WHERE email = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, email).stream().findFirst();
    }

    public Optional<InternalUser> findById(Long id) {
        String sql = """
                SELECT id, first_name, last_name, email, phone, password_hash, role, status, created_at, updated_at
                FROM users
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id).stream().findFirst();
    }

    public List<InternalUser> findAll() {
        String sql = """
                SELECT id, first_name, last_name, email, phone, password_hash, role, status, created_at, updated_at
                FROM users
                ORDER BY created_at DESC, id DESC
                """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    public InternalUser create(CreateUserCommand command) throws DuplicateKeyException {
        String sql = """
                INSERT INTO users (first_name, last_name, email, phone, password_hash, role, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, command.firstName());
            statement.setString(2, command.lastName());
            statement.setString(3, command.email());
            statement.setString(4, command.phone());
            statement.setString(5, command.passwordHash());
            statement.setString(6, command.role().name());
            statement.setString(7, command.status().name());
            return statement;
        }, keyHolder);

        return findById(keyHolder.getKey().longValue()).orElseThrow();
    }

    public boolean updatePartial(Long id, UpdateUserCommand command) {
        String sql = """
                UPDATE users
                SET role = COALESCE(?, role),
                    status = COALESCE(?, status)
                WHERE id = ?
                """;
        return jdbcTemplate.update(
                sql,
                command.role() == null ? null : command.role().name(),
                command.status() == null ? null : command.status().name(),
                id
        ) > 0;
    }

    public record CreateUserCommand(
            String firstName,
            String lastName,
            String email,
            String phone,
            String passwordHash,
            UserRole role,
            UserStatus status
    ) {
    }

    public record UpdateUserCommand(
            UserRole role,
            UserStatus status
    ) {
    }

    private InternalUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new InternalUser(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role")),
                UserStatus.valueOf(rs.getString("status")),
                toLocalDateTime(rs.getTimestamp("created_at")),
                toLocalDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
