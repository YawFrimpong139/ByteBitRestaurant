package org.codewithzea.orderservice.service;


import org.codewithzea.orderservice.dto.OrderItemResponse;
import org.codewithzea.orderservice.dto.OrderResponse;
import org.codewithzea.orderservice.exceptions.OrderNotFoundException;
import org.codewithzea.orderservice.model.Order;
import org.codewithzea.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public OrderResponse getOrderById(UUID id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public Page<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    public Page<OrderResponse> getRestaurantOrders(UUID restaurantId, Pageable pageable) {
        return orderRepository.findByRestaurantId(restaurantId, pageable)
                .map(this::mapToResponse);
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
