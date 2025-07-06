package org.codewithzea.orderservice.controller;


import org.codewithzea.orderservice.dto.OrderRequest;
import org.codewithzea.orderservice.dto.OrderResponse;
import org.codewithzea.orderservice.service.OrderCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Commands", description = "Endpoints for order processing")
public class OrderController {

    private final OrderCommandService orderCommandService;

    @Operation(
            summary = "Create new order",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID customerId = UUID.fromString(jwt.getSubject());
        return orderCommandService.createOrder(request, customerId);
    }
}
