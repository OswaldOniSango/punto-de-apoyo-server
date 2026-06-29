package com.puntodeapoyo.users.dto;

import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.model.UserStatus;

public record UpdateInternalUserRequest(
        UserRole role,
        UserStatus status
) {

    public boolean hasUpdates() {
        return role != null || status != null;
    }
}
