package com.puntodeapoyo.users.dto;

import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInternalUserRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es requerido")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email debe tener un formato valido")
        @Size(max = 180, message = "El email no puede superar 180 caracteres")
        String email,

        @Size(max = 40, message = "El telefono no puede superar 40 caracteres")
        String phone,

        @NotBlank(message = "La password es requerida")
        @Size(min = 8, max = 100, message = "La password debe tener entre 8 y 100 caracteres")
        String password,

        @NotNull(message = "El rol es requerido")
        UserRole role,

        @NotNull(message = "El estado es requerido")
        UserStatus status
) {
}
