package org.codewithzea.restaurantservice.service;

import io.micrometer.core.annotation.Timed;
import org.codewithzea.restaurantservice.dto.request.AddressRequest;
import org.codewithzea.restaurantservice.dto.request.RestaurantRequest;
import org.codewithzea.restaurantservice.dto.response.AddressResponse;
import org.codewithzea.restaurantservice.dto.response.MenuItemResponse;
import org.codewithzea.restaurantservice.dto.response.RestaurantResponse;
import org.codewithzea.restaurantservice.event.RestaurantEventPublisher;
import org.codewithzea.restaurantservice.exception.*;
import org.codewithzea.restaurantservice.model.*;
import org.codewithzea.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final RestaurantEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable, String search) {
        MDC.put("operation", "getAllRestaurants");
        log.info("Fetching restaurants with search: '{}' and pageable: {}", search, pageable);

        try {
            Page<RestaurantResponse> result;
            if (search != null && !search.isBlank()) {
                result = restaurantRepository.findByNameContainingIgnoreCase(search, pageable)
                        .map(this::mapToResponse);
                log.debug("Found {} restaurants matching search '{}'", result.getTotalElements(), search);
            } else {
                result = restaurantRepository.findAll(pageable)
                        .map(this::mapToResponse);
                log.debug("Found {} total restaurants", result.getTotalElements());
            }
            return result;
        } finally {
            MDC.clear();
        }
    }

    @Cacheable(value = "restaurants", key = "#id")
    @Timed(value = "restaurant.service.time", description = "Time taken to get restaurant")
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(UUID id) {
        MDC.put("operation", "getRestaurantById");
        MDC.put("restaurantId", id.toString());
        log.info("Fetching restaurant by ID");

        try {
            RestaurantResponse response = restaurantRepository.findById(id)
                    .map(this::mapToResponse)
                    .orElseThrow(() -> {
                        log.warn("Restaurant not found with ID: {}", id);
                        return new RestaurantNotFoundException(id);
                    });
            log.debug("Successfully retrieved restaurant: {}", response.name());
            return response;
        } finally {
            MDC.clear();
        }
    }

    @Timed(value = "restaurant.service.time", description = "Time taken to create restaurant")
    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        MDC.put("operation", "createRestaurant");
        MDC.put("restaurantName", request.name());
        String ownerId = getCurrentUserId();
        MDC.put("ownerId", ownerId);
        log.info("Creating new restaurant");

        try {
            Restaurant restaurant = Restaurant.builder()
                    .name(request.name())
                    .email(request.email())
                    .phone(request.phone())
                    .address(mapAddress(request.address()))
                    .ownerId(ownerId)
                    .build();

            log.debug("Restaurant entity prepared: {}", restaurant);
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);
            log.info("Successfully created restaurant with ID: {}", savedRestaurant.getId());

            eventPublisher.publishRestaurantCreatedEvent(savedRestaurant);
            log.debug("Published restaurant created event");

            return mapToResponse(savedRestaurant);
        } catch (Exception e) {
            log.error("Failed to create restaurant: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @CacheEvict(value = "restaurants", key = "#id")
    @Transactional
    public void updateRestaurant(UUID id, RestaurantRequest request) {
        MDC.put("operation", "updateRestaurant");
        MDC.put("restaurantId", id.toString());
        String ownerId = getCurrentUserId();
        MDC.put("ownerId", ownerId);
        log.info("Updating restaurant");

        try {
            if (!restaurantRepository.existsByIdAndOwnerId(id, ownerId)) {
                log.warn("Unauthorized update attempt by user {} for restaurant {}", ownerId, id);
                throw new UnauthorizedAccessException("User not authorized to update this restaurant");
            }

            Restaurant restaurant = restaurantRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Restaurant not found with ID: {}", id);
                        return new RestaurantNotFoundException(id);
                    });

            log.debug("Updating restaurant from: {} to: {}", restaurant, request);
            restaurant.setName(request.name());
            restaurant.setEmail(request.email());
            restaurant.setPhone(request.phone());
            restaurant.setAddress(mapAddress(request.address()));

            Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
            log.info("Successfully updated restaurant with ID: {}", id);

            eventPublisher.publishRestaurantUpdatedEvent(updatedRestaurant);
            log.debug("Published restaurant updated event");
        } catch (Exception e) {
            log.error("Failed to update restaurant: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @CacheEvict(value = "restaurants", key = "#id")
    @Transactional
    public void deleteRestaurant(UUID id) {
        MDC.put("operation", "deleteRestaurant");
        MDC.put("restaurantId", id.toString());
        String ownerId = getCurrentUserId();
        MDC.put("ownerId", ownerId);
        log.info("Deleting restaurant");

        try {
            if (!restaurantRepository.existsByIdAndOwnerId(id, ownerId)) {
                log.warn("Unauthorized delete attempt by user {} for restaurant {}", ownerId, id);
                throw new UnauthorizedAccessException("User not authorized to delete this restaurant");
            }

            restaurantRepository.deleteById(id);
            log.info("Successfully deleted restaurant with ID: {}", id);

            eventPublisher.publishRestaurantDeletedEvent(id);
            log.debug("Published restaurant deleted event");
        } catch (Exception e) {
            log.error("Failed to delete restaurant: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    // Private helper methods with debug logging
    private String getCurrentUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Retrieved current user ID: {}", userId);
        return userId;
    }

    private Address mapAddress(AddressRequest addressRequest) {
        Address address = Address.builder()
                .street(addressRequest.street())
                .city(addressRequest.city())
                .state(addressRequest.state())
                .zipCode(addressRequest.zipCode())
                .country(addressRequest.country())
                .build();
        log.debug("Mapped address request to entity: {}", address);
        return address;
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse(
                restaurant.getId().toString(),
                restaurant.getName(),
                restaurant.getEmail(),
                restaurant.getPhone(),
                new AddressResponse(
                        restaurant.getAddress().getStreet(),
                        restaurant.getAddress().getCity(),
                        restaurant.getAddress().getState(),
                        restaurant.getAddress().getZipCode(),
                        restaurant.getAddress().getCountry()
                ),
                restaurant.getMenuItems().stream()
                        .map(this::mapToMenuItemResponse)
                        .toList(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
        log.debug("Mapped restaurant entity to response: {}", response);
        return response;
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId().toString(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory() != null ? menuItem.getCategory().getName() : null
        );
    }
}