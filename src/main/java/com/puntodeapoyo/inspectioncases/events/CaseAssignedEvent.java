package com.puntodeapoyo.inspectioncases.events;

import java.time.Instant;
import java.util.List;

public record CaseAssignedEvent(
        Long caseId,
        String trackingCode,
        List<Long> engineerIds,
        Long assignedByUserId,
        Instant occurredAt
) {

    public static CaseAssignedEvent now(
            Long caseId,
            String trackingCode,
            List<Long> engineerIds,
            Long assignedByUserId
    ) {
        return new CaseAssignedEvent(caseId, trackingCode, List.copyOf(engineerIds), assignedByUserId, Instant.now());
    }
}
