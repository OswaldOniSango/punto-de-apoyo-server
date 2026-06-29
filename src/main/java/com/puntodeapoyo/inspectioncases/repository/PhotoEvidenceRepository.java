package com.puntodeapoyo.inspectioncases.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.puntodeapoyo.inspectioncases.model.PhotoEvidence;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PhotoEvidenceRepository {

    private final JdbcTemplate jdbcTemplate;

    public PhotoEvidenceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PhotoEvidence create(CreatePhotoEvidenceCommand command) {
        String sql = """
                INSERT INTO photo_evidence (
                    case_id,
                    uploaded_by_user_id,
                    public_upload,
                    file_name,
                    file_url,
                    content_type,
                    size_bytes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, command.caseId());
            if (command.uploadedByUserId() == null) {
                statement.setObject(2, null);
            } else {
                statement.setLong(2, command.uploadedByUserId());
            }
            statement.setBoolean(3, command.publicUpload());
            statement.setString(4, command.fileName());
            statement.setString(5, command.fileUrl());
            statement.setString(6, command.contentType());
            statement.setLong(7, command.sizeBytes());
            return statement;
        }, keyHolder);

        return findById(keyHolder.getKey().longValue());
    }

    public PhotoEvidence findById(Long id) {
        String sql = """
                SELECT id, case_id, uploaded_by_user_id, public_upload, file_name, file_url,
                       content_type, size_bytes, created_at
                FROM photo_evidence
                WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sql, this::mapRow, id);
    }

    public List<PhotoEvidence> findByCaseId(Long caseId) {
        String sql = """
                SELECT id, case_id, uploaded_by_user_id, public_upload, file_name, file_url,
                       content_type, size_bytes, created_at
                FROM photo_evidence
                WHERE case_id = ?
                ORDER BY created_at ASC, id ASC
                """;
        return jdbcTemplate.query(sql, this::mapRow, caseId);
    }

    public List<PhotoEvidence> findByCaseIds(List<Long> caseIds) {
        if (caseIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(caseIds.size(), "?"));
        String sql = """
                SELECT id, case_id, uploaded_by_user_id, public_upload, file_name, file_url,
                       content_type, size_bytes, created_at
                FROM photo_evidence
                WHERE case_id IN (%s)
                ORDER BY created_at ASC, id ASC
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, this::mapRow, caseIds.toArray());
    }

    private PhotoEvidence mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long uploadedByUserId = rs.getObject("uploaded_by_user_id", Long.class);
        return new PhotoEvidence(
                rs.getLong("id"),
                rs.getLong("case_id"),
                uploadedByUserId,
                rs.getBoolean("public_upload"),
                rs.getString("file_name"),
                rs.getString("file_url"),
                rs.getString("content_type"),
                rs.getLong("size_bytes"),
                toLocalDateTime(rs.getTimestamp("created_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public record CreatePhotoEvidenceCommand(
            Long caseId,
            Long uploadedByUserId,
            boolean publicUpload,
            String fileName,
            String fileUrl,
            String contentType,
            Long sizeBytes
    ) {
    }
}
