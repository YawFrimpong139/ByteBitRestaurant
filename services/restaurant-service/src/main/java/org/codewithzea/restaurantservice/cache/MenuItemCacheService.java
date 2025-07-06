package org.codewithzea.restaurantservice.cache;


import org.codewithzea.restaurantservice.dto.response.MenuItemResponse;
import org.codewithzea.restaurantservice.model.MenuItem;
import org.codewithzea.restaurantservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemCacheService {
    private final MenuItemRepository menuItemRepository;

    @Cacheable(value = "menuItems", key = "#restaurantId")
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(UUID restaurantId) {
        return menuItemRepository.findByRestaurantIdWithCategories(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "menuItemsPaginated",
            key = "{#restaurantId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getMenuItemsPaginated(UUID restaurantId, Pageable pageable) {
        return menuItemRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToResponse);
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        return MenuItemResponse.builder()
                .id(menuItem.getId().toString())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .categoryName(menuItem.getCategory() != null ?
                        menuItem.getCategory().getName() : null)
                .build();
    }
}