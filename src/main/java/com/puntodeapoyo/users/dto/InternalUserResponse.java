package com.puntodeapoyo.users.dto;

import java.time.LocalDateTime;

import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;

public record InternalUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static InternalUserResponse from(InternalUser user) {
        return new InternalUserResponse(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.email(),
                user.phone(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
