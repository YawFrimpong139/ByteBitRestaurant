package org.codewithzea.orderservice.dto;


import org.codewithzea.orderservice.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        UUID restaurantId,
        List<OrderItemResponse> items,
        OrderStatus status,
        String paymentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

