package com.puntodeapoyo.inspectioncases.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InspectionCase(
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
        LocalDateTime updatedAt
) {
}
