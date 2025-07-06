package org.codewithzea.orderservice.controller;


import org.codewithzea.orderservice.dto.OrderResponse;
import org.codewithzea.orderservice.exceptions.UnauthorizedAccessException;
import org.codewithzea.orderservice.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Queries", description = "Endpoints for order retrieval")
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    @Operation(
            summary = "Get order by ID",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{id}")
    public OrderResponse getOrderById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        OrderResponse order = orderQueryService.getOrderById(id);

        // Verify ownership
        if (!order.customerId().equals(customerId)) {
            throw new UnauthorizedAccessException("order", "view");
        }

        return order;
    }

    @Operation(
            summary = "Get customer orders",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/customer")
    public Page<OrderResponse> getCustomerOrders(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        return orderQueryService.getCustomerOrders(customerId, pageable);
    }

    @Operation(
            summary = "Get restaurant orders",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public Page<OrderResponse> getRestaurantOrders(
            @PathVariable UUID restaurantId,
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ) {
        String ownerId = jwt.getSubject();
        // Additional verification that user owns the restaurant
        // Should be implemented in RestaurantClient
        return orderQueryService.getRestaurantOrders(restaurantId, pageable);
    }
}
