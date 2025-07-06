package org.codewithzea.restaurantservice.dto.response;

import lombok.Builder;
import org.codewithzea.restaurantservice.model.Category;

import java.math.BigDecimal;

@Builder
public record MenuItemResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String categoryName
) {}
