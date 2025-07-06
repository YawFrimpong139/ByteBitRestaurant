package org.codewithzea.orderservice.exceptions;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Standard error response structure
    private Map<String, Object> buildErrorResponse(HttpStatus status, String message, Map<String, Object> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }

        return body;
    }

    // Handle validation errors from @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        Map<String, Object> body = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                Map.of("errors", errors)
        );

        return new ResponseEntity<>(body, headers, status);
    }

    // Handle constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<String> errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        Map<String, Object> body = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                Map.of("violations", errors)
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Custom exception handlers
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handleOrderNotFound(
            OrderNotFoundException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), null));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Object> handlePaymentFailed(
            PaymentFailedException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(buildErrorResponse(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), null));
    }

    @ExceptionHandler(RefundFailedException.class)
    public ResponseEntity<Object> handleRefundFailed(
            RefundFailedException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null));
    }

    @ExceptionHandler(RestaurantNotAvailableException.class)
    public ResponseEntity<Object> handleRestaurantNotAvailable(
            RestaurantNotAvailableException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), null));
    }

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<Object> handleIdempotencyConflict(
            IdempotencyException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null));
    }

    // Fallback handler for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {

        String message = "An unexpected error occurred";
        Map<String, Object> details = null;

        // Include more details for debugging in non-production environments
        if (request.getUserPrincipal() != null && request.getUserPrincipal().getName().equals("admin")) {
            details = Map.of(
                    "exception", ex.getClass().getName(),
                    "rootCause", ex.getCause() != null ? ex.getCause().getMessage() : "none"
            );
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, details));
    }
}
