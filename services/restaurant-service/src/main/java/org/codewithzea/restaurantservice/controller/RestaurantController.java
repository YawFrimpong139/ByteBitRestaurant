package org.codewithzea.restaurantservice.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codewithzea.restaurantservice.dto.request.RestaurantRequest;
import org.codewithzea.restaurantservice.dto.response.RestaurantResponse;
import org.codewithzea.restaurantservice.service.AsyncRestaurantService;
import org.codewithzea.restaurantservice.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RateLimiter(name = "restaurantApi")
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant management operations")
public class RestaurantController {
    private static final Logger log = LoggerFactory.getLogger(RestaurantController.class);

    private final RestaurantService restaurantService;
    private final AsyncRestaurantService asyncRestaurantService;

    @Operation(summary = "Get paginated list of restaurants")
    @GetMapping
    @ResponseBody
    @Cacheable("restaurantsList")
    @Timed(value = "restaurant.controller.time", description = "Time taken to get restaurants")
    public Page<RestaurantResponse> getAllRestaurants(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("operation", "getAllRestaurants");
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Fetching restaurants page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
            if (search != null) {
                log.debug("Searching restaurants with query: '{}'", search);
            }

            Page<RestaurantResponse> response = restaurantService.getAllRestaurants(pageable, search);
            log.debug("Found {} restaurants", response.getTotalElements());

            return response;
        } catch (Exception e) {
            log.error("Failed to fetch restaurants: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Operation(summary = "Get restaurant by ID")
    @GetMapping("/{id}")
    @ResponseBody
    @Cacheable(value = "restaurant", key = "#id")
    @Timed(value = "restaurant.controller.time", description = "Time taken to get restaurant by ID")
    @Retry(name = "restaurantApi", fallbackMethod = "getRestaurantFallback")
    public CompletableFuture<RestaurantResponse> getRestaurantById(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("operation", "getRestaurantById");
        MDC.put("restaurantId", id.toString());
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Fetching restaurant by ID");
            return asyncRestaurantService.getRestaurantAsync(id)
                    .whenComplete((response, ex) -> {
                        if (ex != null) {
                            log.error("Async operation failed: {}", ex.getMessage());
                        } else {
                            log.debug("Successfully retrieved restaurant: {}", response.name());
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to initiate restaurant fetch: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Operation(
            summary = "Create new restaurant",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @CacheEvict(value = {"restaurantsList", "restaurant"}, allEntries = true)
    @Timed(value = "restaurant.controller.time", description = "Time taken to create restaurant")
    public RestaurantResponse createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("operation", "createRestaurant");
        MDC.put("restaurantName", request.name());
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Creating new restaurant: {}", request.name());
            RestaurantResponse response = restaurantService.createRestaurant(request);
            log.info("Successfully created restaurant with ID: {}", response.id());
            return response;
        } catch (Exception e) {
            log.error("Failed to create restaurant: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Operation(
            summary = "Update restaurant",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @CacheEvict(value = {"restaurant", "restaurantsList"}, key = "#id")
    @Timed(value = "restaurant.controller.time", description = "Time taken to update restaurant")
    public CompletableFuture<Void> updateRestaurant(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("operation", "updateRestaurant");
        MDC.put("restaurantId", id.toString());
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Updating restaurant {} with data: {}", id, request);
            return asyncRestaurantService.updateRestaurantAsync(id, request)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to update restaurant: {}", ex.getMessage());
                        } else {
                            log.info("Successfully updated restaurant {}", id);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to initiate restaurant update: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    // Fallback method for Resilience4j Retry
    private CompletableFuture<RestaurantResponse> getRestaurantFallback(
            UUID id,
            Exception ex,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("operation", "getRestaurantFallback");
        MDC.put("restaurantId", id.toString());
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.warn("Using fallback method for restaurant {} due to: {}", id, ex.getMessage());
            return CompletableFuture.completedFuture(
                    new RestaurantResponse(
                            id.toString(),
                            "Fallback Restaurant",
                            "fallback@example.com",
                            "+0000000000",
                            null,
                            List.of(),
                            null,
                            null
                    )
            );
        } finally {
            MDC.clear();
        }
    }
}