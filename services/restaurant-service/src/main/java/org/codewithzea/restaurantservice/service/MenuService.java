package org.codewithzea.restaurantservice.service;

import org.codewithzea.restaurantservice.cache.MenuItemCacheService;
import org.codewithzea.restaurantservice.dto.request.MenuItemRequest;
import org.codewithzea.restaurantservice.dto.response.MenuItemResponse;
import org.codewithzea.restaurantservice.event.MenuEventPublisher;
import org.codewithzea.restaurantservice.exception.*;
import org.codewithzea.restaurantservice.model.MenuItem;
import org.codewithzea.restaurantservice.model.Restaurant;
import org.codewithzea.restaurantservice.repository.MenuItemRepository;
import org.codewithzea.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MenuService {
    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuEventPublisher eventPublisher;
    private final MenuItemCacheService menuItemCacheService;

    @CacheEvict(value = {"menuItems", "menuItemsPaginated", "restaurants"}, key = "#restaurantId")
    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100))
    public MenuItemResponse addMenuItem(UUID restaurantId, MenuItemRequest request) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("menuItemName", request.name());
        log.info("Attempting to add new menu item to restaurant");

        try {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> {
                        log.warn("Restaurant not found with ID: {}", restaurantId);
                        return new RestaurantNotFoundException(restaurantId);
                    });

            verifyOwnership(restaurant);

            MenuItem menuItem = MenuItem.builder()
                    .name(request.name())
                    .description(request.description())
                    .price(request.price())
                    .restaurant(restaurant)
                    .build();

            log.debug("Creating menu item: {}", menuItem);
            MenuItem savedItem = menuItemRepository.save(menuItem);

            log.info("Successfully created menu item with ID: {}", savedItem.getId());
            eventPublisher.publishMenuItemCreatedEvent(savedItem);
            log.debug("Published menu item created event");

            return mapToResponse(savedItem);
        } catch (Exception e) {
            log.error("Failed to add menu item to restaurant {}: {}", restaurantId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @CacheEvict(value = {"menuItems", "menuItemsPaginated", "restaurants"}, key = "#restaurantId")
    @Transactional
    public MenuItemResponse updateMenuItem(UUID restaurantId, UUID itemId, MenuItemRequest request) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("menuItemId", itemId.toString());
        log.info("Attempting to update menu item");

        try {
            MenuItem menuItem = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> {
                        log.warn("Menu item not found with ID: {}", itemId);
                        return new MenuItemNotFoundException(itemId);
                    });

            if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
                log.warn("Menu item {} doesn't belong to restaurant {}", itemId, restaurantId);
                throw new MenuItemNotInRestaurantException(itemId, restaurantId);
            }

            verifyOwnership(menuItem.getRestaurant());

            log.debug("Updating menu item from: {} to: {}", menuItem, request);
            menuItem.setName(request.name());
            menuItem.setDescription(request.description());
            menuItem.setPrice(request.price());

            MenuItem updatedItem = menuItemRepository.save(menuItem);
            log.info("Successfully updated menu item with ID: {}", itemId);

            eventPublisher.publishMenuItemUpdatedEvent(updatedItem);
            log.debug("Published menu item updated event");

            return mapToResponse(updatedItem);
        } catch (Exception e) {
            log.error("Failed to update menu item {}: {}", itemId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @CacheEvict(value = {"menuItems", "menuItemsPaginated", "restaurants"}, key = "#restaurantId")
    @Transactional
    public void deleteMenuItem(UUID restaurantId, UUID itemId) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("menuItemId", itemId.toString());
        log.info("Attempting to delete menu item");

        try {
            MenuItem menuItem = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> {
                        log.warn("Menu item not found with ID: {}", itemId);
                        return new MenuItemNotFoundException(itemId);
                    });

            if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
                log.warn("Menu item {} doesn't belong to restaurant {}", itemId, restaurantId);
                throw new MenuItemNotInRestaurantException(itemId, restaurantId);
            }

            verifyOwnership(menuItem.getRestaurant());

            menuItemRepository.delete(menuItem);
            log.info("Successfully deleted menu item with ID: {}", itemId);

            eventPublisher.publishMenuItemDeletedEvent(itemId, restaurantId);
            log.debug("Published menu item deleted event");
        } catch (Exception e) {
            log.error("Failed to delete menu item {}: {}", itemId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<MenuItemResponse>> getMenuItemsAsync(UUID restaurantId) {
        MDC.put("restaurantId", restaurantId.toString());
        log.info("Fetching menu items asynchronously for restaurant");

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<MenuItemResponse> items = menuItemCacheService.getMenuItems(restaurantId);
                log.debug("Retrieved {} menu items for restaurant {}", items.size(), restaurantId);
                return items;
            } catch (Exception e) {
                log.error("Failed to fetch menu items for restaurant {}: {}", restaurantId, e.getMessage(), e);
                throw e;
            } finally {
                MDC.clear();
            }
        });
    }

    private void verifyOwnership(Restaurant restaurant) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        MDC.put("userId", currentUserId);

        if (!restaurant.getOwnerId().equals(currentUserId)) {
            log.warn("User {} attempted to modify restaurant {} without ownership",
                    currentUserId, restaurant.getId());
            throw new UnauthorizedAccessException("User not authorized to modify this restaurant's menu");
        }
        log.debug("Verified ownership for restaurant {}", restaurant.getId());
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId().toString(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory() != null ? menuItem.getCategory().getName() : null
        );
    }
}