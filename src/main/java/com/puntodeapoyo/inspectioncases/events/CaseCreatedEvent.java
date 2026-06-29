package com.puntodeapoyo.inspectioncases.events;

import java.time.Instant;

public record CaseCreatedEvent(
        Long caseId,
        String trackingCode,
        Instant occurredAt
) {

    public static CaseCreatedEvent now(Long caseId, String trackingCode) {
        return new CaseCreatedEvent(caseId, trackingCode, Instant.now());
    }
}
