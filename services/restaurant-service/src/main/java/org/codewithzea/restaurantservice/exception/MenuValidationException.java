package org.codewithzea.restaurantservice.exception;


import java.util.Set;
import jakarta.validation.ConstraintViolation;

public class MenuValidationException extends RuntimeException {
    private final Set<? extends ConstraintViolation<?>> violations;

    public MenuValidationException(String message, Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations;
    }

    public Set<? extends ConstraintViolation<?>> getViolations() {
        return violations;
    }
}
