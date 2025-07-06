package org.codewithzea.restaurantservice.service;


import org.codewithzea.restaurantservice.dto.request.RestaurantRequest;
import org.codewithzea.restaurantservice.dto.response.RestaurantResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncRestaurantService {

    private final RestaurantService restaurantService;

    public AsyncRestaurantService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Async
    public CompletableFuture<RestaurantResponse> getRestaurantAsync(UUID id) {
        return CompletableFuture.completedFuture(restaurantService.getRestaurantById(id));
    }

    @Async
    public CompletableFuture<Void> updateRestaurantAsync(UUID id, RestaurantRequest request) {
        restaurantService.updateRestaurant(id, request);
        return CompletableFuture.completedFuture(null);
    }
}
