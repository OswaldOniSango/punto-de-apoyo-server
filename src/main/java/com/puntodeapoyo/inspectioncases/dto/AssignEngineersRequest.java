package com.puntodeapoyo.inspectioncases.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AssignEngineersRequest(
        @NotEmpty(message = "Debe enviar al menos un ingeniero")
        List<@NotNull(message = "El id del ingeniero es obligatorio") Long> engineerIds
) {
}
