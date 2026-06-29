package com.puntodeapoyo.inspectioncases.dto;

import java.time.LocalDateTime;

import com.puntodeapoyo.inspectioncases.model.PhotoEvidence;

public record PhotoEvidenceResponse(
        Long id,
        Long caseId,
        Long uploadedByUserId,
        boolean publicUpload,
        String fileName,
        String fileUrl,
        String contentType,
        Long sizeBytes,
        LocalDateTime createdAt
) {

    public static PhotoEvidenceResponse from(PhotoEvidence photoEvidence) {
        return new PhotoEvidenceResponse(
                photoEvidence.id(),
                photoEvidence.caseId(),
                photoEvidence.uploadedByUserId(),
                photoEvidence.publicUpload(),
                photoEvidence.fileName(),
                photoEvidence.fileUrl(),
                photoEvidence.contentType(),
                photoEvidence.sizeBytes(),
                photoEvidence.createdAt()
        );
    }
}
