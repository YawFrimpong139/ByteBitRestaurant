package org.codewithzea.restaurantservice.exception;


import java.util.UUID;

public class MenuItemNotInRestaurantException extends RuntimeException {
    public MenuItemNotInRestaurantException(UUID itemId, UUID restaurantId) {
        super(String.format(
                "Menu item %s does not belong to restaurant %s",
                itemId, restaurantId
        ));
    }
}