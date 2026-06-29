package com.puntodeapoyo.inspectioncases.dto;

import com.puntodeapoyo.inspectioncases.model.StructuralRisk;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTechnicalObservationRequest(
        @NotBlank(message = "Las observaciones son obligatorias")
        @Size(max = 5000, message = "Las observaciones no pueden superar 5000 caracteres")
        String observations,

        @NotBlank(message = "Las recomendaciones son obligatorias")
        @Size(max = 5000, message = "Las recomendaciones no pueden superar 5000 caracteres")
        String recommendations,

        @NotNull(message = "El riesgo estructural es obligatorio")
        StructuralRisk structuralRisk
) {
}
