package com.puntodeapoyo.inspectioncases.model;

import java.time.LocalDateTime;

public record TechnicalObservation(
        Long id,
        Long caseId,
        Long createdByUserId,
        String observations,
        String recommendations,
        StructuralRisk structuralRisk,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
