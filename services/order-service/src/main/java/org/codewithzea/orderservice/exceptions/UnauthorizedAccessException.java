package org.codewithzea.orderservice.exceptions;


public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String resource, String action) {
        super(String.format("User not authorized to %s %s", action, resource));
    }

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
