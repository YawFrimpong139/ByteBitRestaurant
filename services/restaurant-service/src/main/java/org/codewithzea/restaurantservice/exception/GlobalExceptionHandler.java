package org.codewithzea.restaurantservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.codewithzea.restaurantservice.dto.response.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(
                ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed",
                        request.getDescription(false),
                        errors,
                        "VALIDATION_ERROR"
                )
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        log.warn("Malformed request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "Malformed request body",
                        request.getDescription(false),
                        List.of(ex.getMostSpecificCause().getMessage()),
                        "MALFORMED_REQUEST"
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        log.warn("Constraint violation: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(
                ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "Constraint violations",
                        request.getDescription(false),
                        errors,
                        "CONSTRAINT_VIOLATION"
                )
        );
    }

    @ExceptionHandler({
            RestaurantNotFoundException.class,
            MenuItemNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(
            RuntimeException ex, WebRequest request) {

        log.info("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.of(
                        HttpStatus.FORBIDDEN,
                        "Access denied",
                        request.getDescription(false),
                        List.of(ex.getMessage()),
                        "ACCESS_DENIED"
                )
        );
    }

    @ExceptionHandler({
            MenuItemNotInRestaurantException.class,
            MenuOptimisticLockException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(
            RuntimeException ex, WebRequest request) {

        log.warn("Conflict detected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(
                        HttpStatus.CONFLICT,
                        ex.getMessage(),
                        request.getDescription(false)
                )
        );
    }

    @ExceptionHandler(MenuValidationException.class)
    public ResponseEntity<ErrorResponse> handleMenuValidation(
            MenuValidationException ex, WebRequest request) {

        log.warn("Business validation failed: {}", ex.getMessage());

        List<String> errors = ex.getViolations() != null ?
                ex.getViolations().stream()
                        .map(this::formatConstraintViolation)
                        .collect(Collectors.toList()) :
                null;

        return ResponseEntity.unprocessableEntity().body(
                ErrorResponse.of(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        ex.getMessage(),
                        request.getDescription(false),
                        errors,
                        "BUSINESS_VALIDATION"
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error occurred", ex);
        return ResponseEntity.internalServerError().body(
                ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred",
                        request.getDescription(false),
                        isDebugMode(request) ? List.of(ex.getMessage()) : null,
                        "INTERNAL_ERROR"
                )
        );
    }

    private String formatFieldError(FieldError error) {
        return String.format("%s: %s (rejected value: %s)",
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue() != null ?
                        error.getRejectedValue().toString() : "null");
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return String.format("%s: %s (invalid value: %s)",
                lastPathSegment(violation.getPropertyPath().toString()),
                violation.getMessage(),
                violation.getInvalidValue() != null ?
                        violation.getInvalidValue().toString() : "null");
    }

    private String lastPathSegment(String path) {
        return path.substring(path.lastIndexOf('.') + 1);
    }

    private boolean isDebugMode(WebRequest request) {
        return false; // Implement based on your environment
    }
}