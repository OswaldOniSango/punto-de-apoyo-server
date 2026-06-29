package com.puntodeapoyo.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldErrorResponse> fieldErrors
) {

    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(Instant.now(), status, error, message, List.of());
    }

    public static ApiErrorResponse of(
            int status,
            String error,
            String message,
            List<FieldErrorResponse> fieldErrors
    ) {
        return new ApiErrorResponse(Instant.now(), status, error, message, fieldErrors);
    }
}
