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

    private final RestaurantRepository restaurantRepository;
    private final RestaurantEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return restaurantRepository.findByNameContainingIgnoreCase(search, pageable)
                    .map(this::mapToResponse);
        }

        return restaurantRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> searchRestaurants(String name, Pageable pageable) {
        return restaurantRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(value = "restaurants", key = "#id")
    @Timed(value = "restaurant.service.time", description = "Time taken to get restaurant")
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(UUID id) {
        return restaurantRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    @Timed(value = "restaurant.service.time", description = "Time taken to create restaurant")
    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        String ownerId = getCurrentUserId();

        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(mapAddress(request.address()))
                .ownerId(ownerId)
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        eventPublisher.publishRestaurantCreatedEvent(savedRestaurant);

        return mapToResponse(savedRestaurant);
    }

    @CacheEvict(value = "restaurants", key = "#id")
    @Transactional
    public void updateRestaurant(UUID id, RestaurantRequest request) {
        String ownerId = getCurrentUserId();

        if (restaurantRepository.existsByIdAndOwnerId(id, ownerId)) {
            throw new UnauthorizedAccessException("User not authorized to update this restaurant");
        }

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        restaurant.setName(request.name());
        restaurant.setEmail(request.email());
        restaurant.setPhone(request.phone());
        restaurant.setAddress(mapAddress(request.address()));

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        eventPublisher.publishRestaurantUpdatedEvent(updatedRestaurant);

        mapToResponse(updatedRestaurant);
    }

    @CacheEvict(value = "restaurants", key = "#id")
    @Transactional
    public void deleteRestaurant(UUID id) {
        String ownerId = getCurrentUserId();

        if (restaurantRepository.existsByIdAndOwnerId(id, ownerId)) {
            throw new UnauthorizedAccessException("User not authorized to delete this restaurant");
        }

        restaurantRepository.deleteById(id);
        eventPublisher.publishRestaurantDeletedEvent(id);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Address mapAddress(AddressRequest addressRequest) {
        return Address.builder()
                .street(addressRequest.street())
                .city(addressRequest.city())
                .state(addressRequest.state())
                .zipCode(addressRequest.zipCode())
                .country(addressRequest.country())
                .build();
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return new RestaurantResponse(
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
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId().toString(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory()!= null ? menuItem.getCategory().getName() : null
        );
    }
}
