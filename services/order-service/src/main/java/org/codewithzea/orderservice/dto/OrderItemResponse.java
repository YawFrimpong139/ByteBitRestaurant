package org.codewithzea.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID menuItemId,
        String name,
        Integer quantity,
        BigDecimal price
) {}
