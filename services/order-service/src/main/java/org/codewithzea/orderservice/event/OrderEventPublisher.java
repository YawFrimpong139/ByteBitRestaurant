package org.codewithzea.orderservice.event;


import org.codewithzea.orderservice.model.Order;
import org.codewithzea.orderservice.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishOrderCreatedEvent(Order order) {
        eventPublisher.publishEvent(
                new OrderEvent(this, order, null, OrderEvent.EventType.CREATED)
        );
    }

    public void publishOrderStatusChangedEvent(Order order, OrderStatus previousStatus) {
        eventPublisher.publishEvent(
                new OrderEvent(this, order, previousStatus, OrderEvent.EventType.STATUS_CHANGED)
        );
    }

    public void publishOrderCancelledEvent(Order order) {
        eventPublisher.publishEvent(
                new OrderEvent(this, order, order.getStatus(), OrderEvent.EventType.CANCELLED)
        );
    }
}