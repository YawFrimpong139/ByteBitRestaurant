package org.codewithzea.restaurantservice.controller;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codewithzea.restaurantservice.cache.MenuItemCacheService;
import org.codewithzea.restaurantservice.dto.request.MenuItemRequest;
import org.codewithzea.restaurantservice.dto.response.MenuItemResponse;
import org.codewithzea.restaurantservice.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@Tag(name = "Menu Items", description = "Manage restaurant menu items")
public class MenuController {
    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    private final MenuService menuService;
    private final MenuItemCacheService menuItemCacheService;

    @Operation(summary = "Get all menu items for a restaurant")
    @GetMapping
    @Timed(value = "menu.controller.time", description = "Time taken to get menu items")
    public List<MenuItemResponse> getMenuItems(
            @PathVariable UUID restaurantId,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("operation", "getMenuItems");
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Fetching menu items for restaurant");
            List<MenuItemResponse> items = menuItemCacheService.getMenuItems(restaurantId);
            log.debug("Found {} menu items for restaurant {}", items.size(), restaurantId);
            return items;
        } catch (Exception e) {
            log.error("Failed to fetch menu items: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Operation(
            summary = "Add menu item to restaurant",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @Timed(value = "menu.controller.time", description = "Time taken to add menu item")
    public MenuItemResponse addMenuItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("operation", "addMenuItem");
        MDC.put("menuItemName", request.name());
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Adding new menu item: {}", request.name());
            MenuItemResponse response = menuService.addMenuItem(restaurantId, request);
            log.info("Successfully added menu item with ID: {}", response.id());
            return response;
        } catch (Exception e) {
            log.error("Failed to add menu item: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Operation(
            summary = "Update menu item",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @Timed(value = "menu.controller.time", description = "Time taken to update menu item")
    public MenuItemResponse updateMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("itemId", itemId.toString());
        MDC.put("operation", "updateMenuItem");
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Updating menu item {} with data: {}", itemId, request);
            MenuItemResponse response = menuService.updateMenuItem(restaurantId, itemId, request);
            log.info("Successfully updated menu item {}", itemId);
            return response;
        } catch (Exception e) {
            log.error("Failed to update menu item {}: {}", itemId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Operation(
            summary = "Delete menu item",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @Timed(value = "menu.controller.time", description = "Time taken to delete menu item")
    public void deleteMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId
    ) {
        MDC.put("restaurantId", restaurantId.toString());
        MDC.put("itemId", itemId.toString());
        MDC.put("operation", "deleteMenuItem");
        if (requestId != null) MDC.put("requestId", requestId);

        try {
            log.info("Deleting menu item {}", itemId);
            menuService.deleteMenuItem(restaurantId, itemId);
            log.info("Successfully deleted menu item {}", itemId);
        } catch (Exception e) {
            log.error("Failed to delete menu item {}: {}", itemId, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}