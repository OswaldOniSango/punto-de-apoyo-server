package com.puntodeapoyo.auth;

public record LoginRequest(
        String email,
        String password
) {
}
