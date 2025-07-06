package org.codewithzea.restaurantservice.event;


import org.codewithzea.restaurantservice.model.Restaurant;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class RestaurantEvent extends ApplicationEvent {
    private final EventType eventType;
    private final UUID restaurantId;
    private final Restaurant restaurant;

    public RestaurantEvent(Object source, EventType eventType, UUID restaurantId, Restaurant restaurant) {
        super(source);
        this.eventType = eventType;
        this.restaurantId = restaurantId;
        this.restaurant = restaurant;
    }

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}
