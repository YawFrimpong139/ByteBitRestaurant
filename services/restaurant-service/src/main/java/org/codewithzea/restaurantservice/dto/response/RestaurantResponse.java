package org.codewithzea.restaurantservice.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public record RestaurantResponse(
        String id,
        String name,
        String email,
        String phone,
        AddressResponse address,
        List<MenuItemResponse> menuItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}


