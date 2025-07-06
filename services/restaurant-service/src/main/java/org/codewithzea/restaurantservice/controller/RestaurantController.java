package org.codewithzea.restaurantservice.controller;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.codewithzea.restaurantservice.dto.request.RestaurantRequest;
import org.codewithzea.restaurantservice.dto.response.RestaurantResponse;
import org.codewithzea.restaurantservice.service.AsyncRestaurantService;
import org.codewithzea.restaurantservice.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RateLimiter(name = "restaurantApi")
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final AsyncRestaurantService asyncRestaurantService;

    @GetMapping
    @ResponseBody
    @Cacheable("restaurantsList")
    public Page<RestaurantResponse> getAllRestaurants(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @RequestParam(required = false) String search
    ) {
        return restaurantService.getAllRestaurants(pageable, search);
    }

    @GetMapping("/{id}")
    @ResponseBody
    @Cacheable(value = "restaurant", key = "#id")
    public CompletableFuture<RestaurantResponse> getRestaurantById(@PathVariable UUID id) {
        return asyncRestaurantService.getRestaurantAsync(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @CacheEvict(value = {"restaurantsList", "restaurant"}, allEntries = true)
    public RestaurantResponse createRestaurant(
            @Valid @RequestBody RestaurantRequest request
    ) {
        return restaurantService.createRestaurant(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @CacheEvict(value = {"restaurant", "restaurantsList"}, key = "#id")
    public CompletableFuture<Void> updateRestaurant(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantRequest request
    ) {
        return asyncRestaurantService.updateRestaurantAsync(id, request);
    }
}
