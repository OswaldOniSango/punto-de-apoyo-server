package com.puntodeapoyo.inspectioncases.dto;

import java.time.LocalDateTime;

import com.puntodeapoyo.inspectioncases.model.CaseAssignment;

public record CaseAssignmentResponse(
        Long id,
        Long engineerId,
        String engineerFirstName,
        String engineerLastName,
        String engineerEmail,
        String engineerPhone,
        Long assignedBy,
        LocalDateTime assignedAt
) {

    public static CaseAssignmentResponse from(CaseAssignment assignment) {
        return new CaseAssignmentResponse(
                assignment.id(),
                assignment.engineerId(),
                assignment.engineerFirstName(),
                assignment.engineerLastName(),
                assignment.engineerEmail(),
                assignment.engineerPhone(),
                assignment.assignedBy(),
                assignment.assignedAt()
        );
    }
}
