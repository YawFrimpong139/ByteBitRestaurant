package org.codewithzea.restaurantservice.dto.response;



import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details,
        String errorType
) {
    public ErrorResponse {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null,
                null
        );
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, List<String> details, String errorType) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details,
                errorType
        );
    }
}
