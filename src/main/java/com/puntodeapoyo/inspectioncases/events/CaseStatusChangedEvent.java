package com.puntodeapoyo.inspectioncases.events;

import java.time.Instant;

import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;

public record CaseStatusChangedEvent(
        Long caseId,
        String trackingCode,
        InspectionCaseStatus previousStatus,
        InspectionCaseStatus newStatus,
        Long changedByUserId,
        Instant occurredAt
) {

    public static CaseStatusChangedEvent now(
            Long caseId,
            String trackingCode,
            InspectionCaseStatus previousStatus,
            InspectionCaseStatus newStatus,
            Long changedByUserId
    ) {
        return new CaseStatusChangedEvent(
                caseId,
                trackingCode,
                previousStatus,
                newStatus,
                changedByUserId,
                Instant.now()
        );
    }
}
