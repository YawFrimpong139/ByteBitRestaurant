package org.codewithzea.orderservice.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotNull UUID restaurantId,
        @NotEmpty List<@Valid OrderItemRequest> items,
        String idempotencyKey
) {}
