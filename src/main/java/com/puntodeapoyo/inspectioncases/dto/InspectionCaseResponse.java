package com.puntodeapoyo.inspectioncases.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.puntodeapoyo.inspectioncases.model.InspectionCase;
import com.puntodeapoyo.inspectioncases.model.InspectionCasePriority;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;

public record InspectionCaseResponse(
        Long id,
        String trackingCode,
        String applicantName,
        String applicantPhone,
        String applicantEmail,
        String address,
        String city,
        String stateRegion,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        InspectionCasePriority priority,
        InspectionCaseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PhotoEvidenceResponse> photos,
        List<CaseAssignmentResponse> assignments
) {

    public static InspectionCaseResponse from(InspectionCase inspectionCase) {
        return from(inspectionCase, List.of(), List.of());
    }

    public static InspectionCaseResponse from(
            InspectionCase inspectionCase,
            List<PhotoEvidenceResponse> photos
    ) {
        return from(inspectionCase, photos, List.of());
    }

    public static InspectionCaseResponse from(
            InspectionCase inspectionCase,
            List<PhotoEvidenceResponse> photos,
            List<CaseAssignmentResponse> assignments
    ) {
        return new InspectionCaseResponse(
                inspectionCase.id(),
                inspectionCase.trackingCode(),
                inspectionCase.applicantName(),
                inspectionCase.applicantPhone(),
                inspectionCase.applicantEmail(),
                inspectionCase.address(),
                inspectionCase.city(),
                inspectionCase.stateRegion(),
                inspectionCase.description(),
                inspectionCase.latitude(),
                inspectionCase.longitude(),
                inspectionCase.priority(),
                inspectionCase.status(),
                inspectionCase.createdAt(),
                inspectionCase.updatedAt(),
                photos,
                assignments
        );
    }
}
