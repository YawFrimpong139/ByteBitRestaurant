package org.codewithzea.restaurantservice.exception;

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
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Standard Exception Format
    private Map<String, Object> createErrorBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }

    // 400 - Bad Request (Validation Errors)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Validation errors");

        // Collect field errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        body.put("errors", errors);
        return new ResponseEntity<>(body, headers, status);
    }

    // 400 - Constraint Violation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Constraint violations");

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        List<String> errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());

        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 404 - Restaurant Not Found
    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<Object> handleRestaurantNotFound(
            RestaurantNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.NOT_FOUND, ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    // 404 - Menu Item Not Found
    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<Object> handleMenuItemNotFound(
            MenuItemNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.NOT_FOUND, ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    // 403 - Unauthorized Access
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, WebRequest request) {
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.FORBIDDEN, ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    // 409 - Menu Item Not in Restaurant
    @ExceptionHandler(MenuItemNotInRestaurantException.class)
    public ResponseEntity<Object> handleMenuItemNotInRestaurant(
            MenuItemNotInRestaurantException ex, WebRequest request) {
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.CONFLICT, ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    // 409 - Optimistic Locking
    @ExceptionHandler(MenuOptimisticLockException.class)
    public ResponseEntity<Object> handleOptimisticLock(
            MenuOptimisticLockException ex, WebRequest request) {
        return new ResponseEntity<>(
                createErrorBody(HttpStatus.CONFLICT, ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    // 422 - Custom Validation
    @ExceptionHandler(MenuValidationException.class)
    public ResponseEntity<Object> handleMenuValidation(
            MenuValidationException ex, WebRequest request) {

        Map<String, Object> body = createErrorBody(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );

        if (ex.getViolations() != null) {
            List<String> errors = ex.getViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toList());
            body.put("errors", errors);
        }

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // 500 - All Other Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex, WebRequest request) {

        String message = "An unexpected error occurred";
        // Don't expose internal details in production
        if (request.getUserPrincipal() != null) { // Debug mode for admins
            message += ": " + ex.getMessage();
        }

        return new ResponseEntity<>(
                createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR, message),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}