package com.puntodeapoyo.inspectioncases.model;

import java.time.LocalDateTime;

public record PhotoEvidence(
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
}
