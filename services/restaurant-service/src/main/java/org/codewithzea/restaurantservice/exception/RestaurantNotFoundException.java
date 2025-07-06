package org.codewithzea.restaurantservice.exception;


import java.util.UUID;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(UUID id) {
        super("Restaurant not found with ID: " + id);
    }

    public RestaurantNotFoundException(String name) {
        super("Restaurant not found with name: " + name);
    }
}
