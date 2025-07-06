package org.codewithzea.orderservice.exceptions;


import java.util.UUID;

public class RestaurantNotAvailableException extends RuntimeException {
    public RestaurantNotAvailableException(UUID restaurantId) {
        super("Restaurant not available with ID: " + restaurantId);
    }
}