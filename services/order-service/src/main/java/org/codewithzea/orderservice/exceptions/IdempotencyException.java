package org.codewithzea.orderservice.exceptions;


public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) {
        super(message);
    }
}
