package org.codewithzea.orderservice.exceptions;


import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID id) {
        super("Order not found with ID: " + id);
    }
}