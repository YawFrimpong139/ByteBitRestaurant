package org.codewithzea.restaurantservice.event;


import org.codewithzea.restaurantservice.model.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishRestaurantCreatedEvent(Restaurant restaurant) {
        eventPublisher.publishEvent(
                new RestaurantEvent(this,
                        RestaurantEvent.EventType.CREATED,
                        restaurant.getId(),
                        restaurant)
        );
    }

    public void publishRestaurantUpdatedEvent(Restaurant restaurant) {
        eventPublisher.publishEvent(
                new RestaurantEvent(this,
                        RestaurantEvent.EventType.UPDATED,
                        restaurant.getId(),
                        restaurant)
        );
    }

    public void publishRestaurantDeletedEvent(UUID restaurantId) {
        eventPublisher.publishEvent(
                new RestaurantEvent(this,
                        RestaurantEvent.EventType.DELETED,
                        restaurantId,
                        null)
        );
    }
}
