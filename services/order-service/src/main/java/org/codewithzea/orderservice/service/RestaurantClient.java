package org.codewithzea.orderservice.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "restaurant-service", url = "${restaurant.service.url}")
public interface RestaurantClient {

    @GetMapping("/api/restaurants/{id}/availability")
    boolean isRestaurantAvailable(@PathVariable UUID id);

    @GetMapping("/api/restaurants/{restaurantId}/is-owner/{userId}")
    boolean isRestaurantOwner(
            @PathVariable UUID restaurantId,
            @PathVariable UUID userId
    );
}
