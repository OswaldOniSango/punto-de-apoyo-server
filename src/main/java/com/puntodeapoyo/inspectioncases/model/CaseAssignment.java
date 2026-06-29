package com.puntodeapoyo.inspectioncases.model;

import java.time.LocalDateTime;

public record CaseAssignment(
        Long id,
        Long caseId,
        Long engineerId,
        String engineerFirstName,
        String engineerLastName,
        String engineerEmail,
        String engineerPhone,
        Long assignedBy,
        LocalDateTime assignedAt
) {
}
