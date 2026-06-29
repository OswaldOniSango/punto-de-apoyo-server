package com.puntodeapoyo.inspectioncases.dto;

import java.math.BigDecimal;

import com.puntodeapoyo.inspectioncases.model.InspectionCasePriority;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInspectionCaseRequest(
        @NotBlank(message = "El nombre del solicitante es requerido")
        @Size(max = 150, message = "El nombre del solicitante no puede superar 150 caracteres")
        String applicantName,

        @NotBlank(message = "El telefono del solicitante es requerido")
        @Size(max = 40, message = "El telefono del solicitante no puede superar 40 caracteres")
        String applicantPhone,

        @Email(message = "El email del solicitante debe tener un formato valido")
        @Size(max = 180, message = "El email del solicitante no puede superar 180 caracteres")
        String applicantEmail,

        @NotBlank(message = "La direccion es requerida")
        @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
        String address,

        @Size(max = 120, message = "La ciudad no puede superar 120 caracteres")
        String city,

        @Size(max = 120, message = "El estado o region no puede superar 120 caracteres")
        String stateRegion,

        @NotBlank(message = "La descripcion del dano es requerida")
        String description,

        @DecimalMin(value = "-90.0000000", message = "La latitud debe ser mayor o igual a -90")
        @DecimalMax(value = "90.0000000", message = "La latitud debe ser menor o igual a 90")
        @Digits(integer = 3, fraction = 7, message = "La latitud debe tener maximo 7 decimales")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0000000", message = "La longitud debe ser mayor o igual a -180")
        @DecimalMax(value = "180.0000000", message = "La longitud debe ser menor o igual a 180")
        @Digits(integer = 3, fraction = 7, message = "La longitud debe tener maximo 7 decimales")
        BigDecimal longitude,

        @NotNull(message = "La prioridad es requerida")
        InspectionCasePriority priority
) {
}
