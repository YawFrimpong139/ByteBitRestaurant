package org.codewithzea.restaurantservice.controller;


import org.codewithzea.restaurantservice.cache.MenuItemCacheService;
import org.codewithzea.restaurantservice.dto.request.MenuItemRequest;
import org.codewithzea.restaurantservice.dto.response.MenuItemResponse;
import org.codewithzea.restaurantservice.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final MenuService menuService;
    private final MenuItemCacheService menuItemCacheService;

    @Operation(summary = "Get all menu items for a restaurant")
    @GetMapping
    public List<MenuItemResponse> getMenuItems(
            @PathVariable UUID restaurantId
    ) {
        return menuItemCacheService.getMenuItems(restaurantId);
    }

    @Operation(
            summary = "Add menu item to restaurant",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public MenuItemResponse addMenuItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return menuService.addMenuItem(restaurantId, request);
    }

    @Operation(
            summary = "Update menu item",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public MenuItemResponse updateMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return menuService.updateMenuItem(restaurantId, itemId, request);
    }

    @Operation(
            summary = "Delete menu item",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public void deleteMenuItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId
    ) {
        menuService.deleteMenuItem(restaurantId, itemId);
    }
}