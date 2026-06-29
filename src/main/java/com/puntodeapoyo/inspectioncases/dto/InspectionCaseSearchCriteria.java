package com.puntodeapoyo.inspectioncases.dto;

import java.time.LocalDate;

import com.puntodeapoyo.inspectioncases.model.InspectionCasePriority;
import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;

public record InspectionCaseSearchCriteria(
        String trackingCode,
        InspectionCaseStatus status,
        String city,
        InspectionCasePriority priority,
        LocalDate createdDate
) {
}
