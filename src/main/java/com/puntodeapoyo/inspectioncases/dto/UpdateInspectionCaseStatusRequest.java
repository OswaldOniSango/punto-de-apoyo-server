package com.puntodeapoyo.inspectioncases.dto;

import com.puntodeapoyo.inspectioncases.model.InspectionCaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInspectionCaseStatusRequest(
        @NotNull(message = "El status es obligatorio")
        InspectionCaseStatus status
) {
}
