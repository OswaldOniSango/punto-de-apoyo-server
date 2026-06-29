package com.puntodeapoyo.inspectioncases.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.puntodeapoyo.inspectioncases.dto.InspectionCaseSearchCriteria;
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

    public Optional<InspectionCase> findByTrackingCodeAndApplicantPhone(String trackingCode, String applicantPhone) {
        String sql = """
                SELECT id, tracking_code, applicant_name, applicant_phone, applicant_email, address, city,
                       state_region, description, latitude, longitude, priority, status, created_at, updated_at
                FROM inspection_cases
                WHERE tracking_code = ?
                  AND applicant_phone = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, trackingCode, applicantPhone).stream().findFirst();
    }

    public List<InspectionCase> search(InspectionCaseSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, tracking_code, applicant_name, applicant_phone, applicant_email, address, city,
                       state_region, description, latitude, longitude, priority, status, created_at, updated_at
                FROM inspection_cases
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (criteria.trackingCode() != null && !criteria.trackingCode().isBlank()) {
            sql.append(" AND tracking_code = ?");
            params.add(criteria.trackingCode().trim());
        }
        if (criteria.status() != null) {
            sql.append(" AND status = ?");
            params.add(criteria.status().name());
        }
        if (criteria.city() != null && !criteria.city().isBlank()) {
            sql.append(" AND LOWER(city) = LOWER(?)");
            params.add(criteria.city().trim());
        }
        if (criteria.priority() != null) {
            sql.append(" AND priority = ?");
            params.add(criteria.priority().name());
        }
        if (criteria.createdDate() != null) {
            LocalDate date = criteria.createdDate();
            sql.append(" AND created_at >= ? AND created_at < ?");
            params.add(Timestamp.valueOf(date.atStartOfDay()));
            params.add(Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
        }

        sql.append(" ORDER BY created_at DESC, id DESC");
        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
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
