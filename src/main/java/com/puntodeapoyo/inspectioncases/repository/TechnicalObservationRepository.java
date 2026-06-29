package com.puntodeapoyo.inspectioncases.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.puntodeapoyo.inspectioncases.model.PhotoEvidence;
import com.puntodeapoyo.inspectioncases.model.StructuralRisk;
import com.puntodeapoyo.inspectioncases.model.TechnicalObservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TechnicalObservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public TechnicalObservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TechnicalObservation create(CreateTechnicalObservationCommand command) {
        String sql = """
                INSERT INTO technical_observations (
                    case_id,
                    created_by_user_id,
                    observations,
                    recommendations,
                    structural_risk
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, command.caseId());
            statement.setLong(2, command.createdByUserId());
            statement.setString(3, command.observations());
            statement.setString(4, command.recommendations());
            statement.setString(5, command.structuralRisk().name());
            return statement;
        }, keyHolder);

        return findById(keyHolder.getKey().longValue());
    }

    public TechnicalObservation findById(Long id) {
        String sql = """
                SELECT id, case_id, created_by_user_id, observations, recommendations,
                       structural_risk, created_at, updated_at
                FROM technical_observations
                WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sql, this::mapObservationRow, id);
    }

    public void linkPhotos(Long technicalObservationId, List<Long> photoEvidenceIds) {
        if (photoEvidenceIds.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO technical_observation_photos (technical_observation_id, photo_evidence_id)
                VALUES (?, ?)
                """;
        jdbcTemplate.batchUpdate(
                sql,
                photoEvidenceIds,
                photoEvidenceIds.size(),
                (statement, photoEvidenceId) -> {
                    statement.setLong(1, technicalObservationId);
                    statement.setLong(2, photoEvidenceId);
                }
        );
    }

    public List<PhotoEvidence> findPhotosByObservationId(Long technicalObservationId) {
        String sql = """
                SELECT pe.id, pe.case_id, pe.uploaded_by_user_id, pe.public_upload, pe.file_name,
                       pe.file_url, pe.content_type, pe.size_bytes, pe.created_at
                FROM technical_observation_photos top
                INNER JOIN photo_evidence pe ON pe.id = top.photo_evidence_id
                WHERE top.technical_observation_id = ?
                ORDER BY top.created_at ASC, top.id ASC
                """;
        return jdbcTemplate.query(sql, this::mapPhotoRow, technicalObservationId);
    }

    private TechnicalObservation mapObservationRow(ResultSet rs, int rowNum) throws SQLException {
        return new TechnicalObservation(
                rs.getLong("id"),
                rs.getLong("case_id"),
                rs.getLong("created_by_user_id"),
                rs.getString("observations"),
                rs.getString("recommendations"),
                StructuralRisk.valueOf(rs.getString("structural_risk")),
                toLocalDateTime(rs.getTimestamp("created_at")),
                toLocalDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private PhotoEvidence mapPhotoRow(ResultSet rs, int rowNum) throws SQLException {
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

    public record CreateTechnicalObservationCommand(
            Long caseId,
            Long createdByUserId,
            String observations,
            String recommendations,
            StructuralRisk structuralRisk
    ) {
    }
}
