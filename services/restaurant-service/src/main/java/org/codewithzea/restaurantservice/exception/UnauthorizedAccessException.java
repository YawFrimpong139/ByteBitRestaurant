package org.codewithzea.restaurantservice.exception;



public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String resource, String action) {
        super(String.format("User not authorized to %s %s", action, resource));
    }
}
