package com.puntodeapoyo.api;

public record FieldErrorResponse(
        String field,
        String message
) {
}
