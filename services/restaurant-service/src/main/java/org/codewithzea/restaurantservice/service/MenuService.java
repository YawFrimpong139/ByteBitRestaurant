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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuEventPublisher eventPublisher;
    private final MenuItemCacheService menuItemCacheService;


    @CacheEvict(value = {"menuItems", "menuItemsPaginated", "restaurants"}, key = "#restaurantId")
    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class},
            backoff = @Backoff(delay = 100))
    public MenuItemResponse addMenuItem(UUID restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        verifyOwnership(restaurant);

        MenuItem menuItem = MenuItem.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .restaurant(restaurant)
                .build();

        MenuItem savedItem = menuItemRepository.save(menuItem);
        eventPublisher.publishMenuItemCreatedEvent(savedItem);

        return mapToResponse(savedItem);
    }

    @CacheEvict(value = {"menuItems", "menuItemsPaginated", "restaurants"}, key = "#restaurantId")
    @Transactional
    public MenuItemResponse updateMenuItem(UUID restaurantId, UUID itemId, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new MenuItemNotFoundException(itemId));

        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new MenuItemNotInRestaurantException(itemId, restaurantId);
        }

        verifyOwnership(menuItem.getRestaurant());

        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        eventPublisher.publishMenuItemUpdatedEvent(updatedItem);

        return mapToResponse(updatedItem);
    }

    @CacheEvict(value = {"menuItems", "menuItemsPaginated", "restaurants"}, key = "#restaurantId")
    @Transactional
    public void deleteMenuItem(UUID restaurantId, UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new MenuItemNotFoundException(itemId));

        if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
            throw new MenuItemNotInRestaurantException(itemId, restaurantId);
        }

        verifyOwnership(menuItem.getRestaurant());

        menuItemRepository.delete(menuItem);
        eventPublisher.publishMenuItemDeletedEvent(itemId, restaurantId);
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<MenuItemResponse>> getMenuItemsAsync(UUID restaurantId) {
        return CompletableFuture.supplyAsync(() -> menuItemCacheService.getMenuItems(restaurantId));
    }

    private void verifyOwnership(Restaurant restaurant) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!restaurant.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("User not authorized to modify this restaurant's menu");
        }
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId().toString(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory()!= null ? menuItem.getCategory().getName() : null
        );
    }
}
