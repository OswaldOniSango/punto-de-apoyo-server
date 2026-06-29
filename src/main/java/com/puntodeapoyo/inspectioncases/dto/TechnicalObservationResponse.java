package com.puntodeapoyo.inspectioncases.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.puntodeapoyo.inspectioncases.model.StructuralRisk;
import com.puntodeapoyo.inspectioncases.model.TechnicalObservation;

public record TechnicalObservationResponse(
        Long id,
        Long caseId,
        Long createdByUserId,
        String observations,
        String recommendations,
        StructuralRisk structuralRisk,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PhotoEvidenceResponse> photos
) {

    public static TechnicalObservationResponse from(
            TechnicalObservation technicalObservation,
            List<PhotoEvidenceResponse> photos
    ) {
        return new TechnicalObservationResponse(
                technicalObservation.id(),
                technicalObservation.caseId(),
                technicalObservation.createdByUserId(),
                technicalObservation.observations(),
                technicalObservation.recommendations(),
                technicalObservation.structuralRisk(),
                technicalObservation.createdAt(),
                technicalObservation.updatedAt(),
                photos
        );
    }
}
