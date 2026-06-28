package com.puntodeapoyo.auth;

import java.time.Instant;

import com.puntodeapoyo.users.model.UserRole;

public record AuthResponse(
        String tokenType,
        String accessToken,
        Instant expiresAt,
        AuthenticatedUser user
) {

    public record AuthenticatedUser(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            UserRole role
    ) {
    }
}
