package org.codewithzea.restaurantservice.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

/**
 * Custom exception for business validation failures in menu operations.
 * Contains constraint violations that caused the validation to fail.
 */
@Getter
public class MenuValidationException extends RuntimeException {
    private final Set<? extends ConstraintViolation<?>> violations;

    /**
     * Creates a new MenuValidationException with a message and violations.
     *
     * @param message the detail message
     * @param violations the set of constraint violations
     */
    public MenuValidationException(String message, Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations != null ? Set.copyOf(violations) : Collections.emptySet();
    }

    /**
     * Creates a new MenuValidationException with just a message.
     *
     * @param message the detail message
     */
    public MenuValidationException(String message) {
        this(message, Collections.emptySet());
    }

    /**
     * Checks if there are any violations associated with this exception.
     *
     * @return true if there are violations, false otherwise
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}