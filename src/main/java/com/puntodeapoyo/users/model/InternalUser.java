package com.puntodeapoyo.users.model;

import java.time.LocalDateTime;

public record InternalUser(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String passwordHash,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
