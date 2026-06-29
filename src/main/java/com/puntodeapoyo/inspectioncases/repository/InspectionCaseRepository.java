package com.puntodeapoyo.inspectioncases.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import com.puntodeapoyo.inspectioncases.model.InspectionCase;
import com.puntodeapoyo.inspectioncases.model.InspectionCasePriority;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class InspectionCaseRepository {

    private final JdbcTemplate jdbcTemplate;

    public InspectionCaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long nextTrackingNumber(int year) {
        jdbcTemplate.update("""
                INSERT INTO inspection_case_counters (year, next_value)
                VALUES (?, 1)
                ON DUPLICATE KEY UPDATE next_value = next_value
                """, year);

        jdbcTemplate.update("""
                UPDATE inspection_case_counters
                SET next_value = LAST_INSERT_ID(next_value + 1)
                WHERE year = ?
                """, year);

        Long nextValue = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (nextValue == null) {
            throw new IllegalStateException("No se pudo generar el numero de tracking");
        }
        return nextValue - 1;
    }

    public InspectionCase create(CreateInspectionCaseCommand command) {
        String sql = """
                INSERT INTO inspection_cases (
                    tracking_code,
                    applicant_name,
                    applicant_phone,
                    applicant_email,
                    address,
                    city,
                    state_region,
                    description,
                    latitude,
                    longitude,
                    priority,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, command.trackingCode());
            statement.setString(2, command.applicantName());
            statement.setString(3, command.applicantPhone());
            statement.setString(4, command.applicantEmail());
            statement.setString(5, command.address());
            statement.setString(6, command.city());
            statement.setString(7, command.stateRegion());
            statement.setString(8, command.description());
            statement.setBigDecimal(9, command.latitude());
            statement.setBigDecimal(10, command.longitude());
            statement.setString(11, command.priority().name());
            statement.setString(12, command.status().name());
            return statement;
        }, keyHolder);

        return findById(keyHolder.getKey().longValue()).orElseThrow();
    }

    public Optional<InspectionCase> findById(Long id) {
        String sql = """
                SELECT id, tracking_code, applicant_name, applicant_phone, applicant_email, address, city,
                       state_region, description, latitude, longitude, priority, status, created_at, updated_at
                FROM inspection_cases
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id).stream().findFirst();
    }

    private InspectionCase mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new InspectionCase(
                rs.getLong("id"),
                rs.getString("tracking_code"),
                rs.getString("applicant_name"),
                rs.getString("applicant_phone"),
                rs.getString("applicant_email"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("state_region"),
                rs.getString("description"),
                rs.getBigDecimal("latitude"),
                rs.getBigDecimal("longitude"),
                InspectionCasePriority.valueOf(rs.getString("priority")),
                InspectionCaseStatus.valueOf(rs.getString("status")),
                toLocalDateTime(rs.getTimestamp("created_at")),
                toLocalDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record CreateInspectionCaseCommand(
            String trackingCode,
            String applicantName,
            String applicantPhone,
            String applicantEmail,
            String address,
            String city,
            String stateRegion,
            String description,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude,
            InspectionCasePriority priority,
            InspectionCaseStatus status
    ) {
    }
}
