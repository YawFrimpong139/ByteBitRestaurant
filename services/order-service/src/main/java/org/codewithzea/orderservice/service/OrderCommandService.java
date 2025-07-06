package org.codewithzea.orderservice.service;


import org.codewithzea.orderservice.dto.OrderItemResponse;
import org.codewithzea.orderservice.dto.OrderRequest;
import org.codewithzea.orderservice.dto.OrderResponse;
import org.codewithzea.orderservice.event.OrderEventPublisher;
import org.codewithzea.orderservice.exceptions.*;
import org.codewithzea.orderservice.model.*;
import org.codewithzea.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final PaymentService paymentService;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    @Retryable(retryFor = RestaurantNotAvailableException.class,
            backoff = @Backoff(delay = 100))
    public OrderResponse createOrder(OrderRequest request, UUID customerId) {
        // Idempotency check
        if (request.idempotencyKey() != null) {
            orderRepository.findByIdempotencyKey(request.idempotencyKey())
                    .ifPresent(order -> {
                        throw new IdempotencyException("Order already exists with idempotency key");
                    });
        }

        // Validate restaurant
        if (!restaurantClient.isRestaurantAvailable(request.restaurantId())) {
            throw new RestaurantNotAvailableException(request.restaurantId());
        }

        // Validate menu items
        BigDecimal totalAmount = request.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.restaurantId())
                .status(OrderStatus.CREATED)
                .idempotencyKey(request.idempotencyKey())
                .build();

        // Add items
        request.items().forEach(item ->
                order.getItems().add(OrderItem.builder()
                        .menuItemId(item.menuItemId())
                        .name(item.name())
                        .quantity(item.quantity())
                        .price(item.price())
                        .order(order)
                        .build())
        );

        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishOrderCreatedEvent(savedOrder);

        // Process payment
        try {
            String paymentId = paymentService.processPayment(totalAmount, customerId);
            savedOrder.setPaymentId(paymentId);
            savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
            orderRepository.save(savedOrder);
        } catch (PaymentFailedException e) {
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(savedOrder);
            throw e;
        }

        return mapToResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);
        eventPublisher.publishOrderStatusChangedEvent(updatedOrder, previousStatus);

        return mapToResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("order", "cancel");
        }

        if (!order.getStatus().equals(OrderStatus.CREATED) &&
                !order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new IllegalStateException("Order cannot be cancelled in its current state");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        eventPublisher.publishOrderCancelledEvent(updatedOrder);

        // Initiate refund if payment was completed
        if (order.getStatus().equals(OrderStatus.PAYMENT_COMPLETED)) {
            paymentService.initiateRefund(order.getPaymentId());
        }

        return mapToResponse(updatedOrder);
    }

    public Optional<Order> findByIdAndUserAccess(UUID orderId, UUID userId) {
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isPresent()) {
            Order existingOrder = order.get();
            boolean isCustomer = userId.equals(existingOrder.getCustomerId());
            boolean isRestaurantOwner = restaurantClient.isRestaurantOwner(
                    existingOrder.getRestaurantId(),
                    userId
            );

            if (isCustomer || isRestaurantOwner) {
                return order;
            }
        }
        return Optional.empty();
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getMenuItemId(),
                                item.getName(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .toList(),
                order.getStatus(),
                order.getPaymentId(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
