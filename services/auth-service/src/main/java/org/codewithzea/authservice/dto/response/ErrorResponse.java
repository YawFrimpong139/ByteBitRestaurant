package org.codewithzea.authservice.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
) {
    public ErrorResponse(int status, String error, String message, Instant timestamp) {
        this(timestamp, status, error, message, null, null);
    }

    public ErrorResponse(int status, String error, String message, Instant timestamp, String path) {
        this(timestamp, status, error, message, path, null);
    }
}