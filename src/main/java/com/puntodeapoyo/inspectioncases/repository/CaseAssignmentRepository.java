package com.puntodeapoyo.inspectioncases.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.puntodeapoyo.inspectioncases.model.CaseAssignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CaseAssignmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CaseAssignmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createMany(Long caseId, List<Long> engineerIds, Long assignedBy) {
        String sql = """
                INSERT IGNORE INTO case_assignments (case_id, engineer_id, assigned_by)
                VALUES (?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(
                sql,
                engineerIds,
                engineerIds.size(),
                (statement, engineerId) -> {
                    statement.setLong(1, caseId);
                    statement.setLong(2, engineerId);
                    statement.setLong(3, assignedBy);
                }
        );
    }

    public List<CaseAssignment> findByCaseId(Long caseId) {
        String sql = """
                SELECT ca.id, ca.case_id, ca.engineer_id, u.first_name, u.last_name, u.email, u.phone,
                       ca.assigned_by, ca.assigned_at
                FROM case_assignments ca
                INNER JOIN users u ON u.id = ca.engineer_id
                WHERE ca.case_id = ?
                ORDER BY ca.assigned_at ASC, ca.id ASC
                """;
        return jdbcTemplate.query(sql, this::mapRow, caseId);
    }

    public List<CaseAssignment> findByCaseIds(List<Long> caseIds) {
        if (caseIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(caseIds.size(), "?"));
        String sql = """
                SELECT ca.id, ca.case_id, ca.engineer_id, u.first_name, u.last_name, u.email, u.phone,
                       ca.assigned_by, ca.assigned_at
                FROM case_assignments ca
                INNER JOIN users u ON u.id = ca.engineer_id
                WHERE ca.case_id IN (%s)
                ORDER BY ca.assigned_at ASC, ca.id ASC
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, this::mapRow, caseIds.toArray());
    }

    private CaseAssignment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CaseAssignment(
                rs.getLong("id"),
                rs.getLong("case_id"),
                rs.getLong("engineer_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getLong("assigned_by"),
                toLocalDateTime(rs.getTimestamp("assigned_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
