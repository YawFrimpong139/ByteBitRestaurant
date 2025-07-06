package org.codewithzea.orderservice.exceptions;


public class RefundFailedException extends RuntimeException {
    public RefundFailedException(String message) {
        super(message);
    }

    public RefundFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}