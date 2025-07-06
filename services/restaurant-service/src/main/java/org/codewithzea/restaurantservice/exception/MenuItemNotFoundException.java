package org.codewithzea.restaurantservice.exception;


import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(UUID itemId) {
        super("Menu item not found with ID: " + itemId);
    }
}
