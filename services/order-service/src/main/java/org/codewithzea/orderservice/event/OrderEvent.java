package org.codewithzea.orderservice.event;


import org.codewithzea.orderservice.model.Order;
import org.codewithzea.orderservice.model.OrderStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderEvent extends ApplicationEvent {
    private final Order order;
    private final OrderStatus previousStatus;
    private final EventType eventType;

    public OrderEvent(Object source, Order order, OrderStatus previousStatus, EventType eventType) {
        super(source);
        this.order = order;
        this.previousStatus = previousStatus;
        this.eventType = eventType;
    }

    public enum EventType {
        CREATED,
        STATUS_CHANGED,
        CANCELLED
    }
}
