package org.codewithzea.restaurantservice.event;


import org.codewithzea.restaurantservice.model.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MenuEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishMenuItemCreatedEvent(MenuItem menuItem) {
        eventPublisher.publishEvent(
                new MenuEvent(this,
                        MenuEvent.EventType.CREATED,
                        menuItem.getId(),
                        menuItem.getRestaurant().getId(),
                        menuItem)
        );
    }

    public void publishMenuItemUpdatedEvent(MenuItem menuItem) {
        eventPublisher.publishEvent(
                new MenuEvent(this,
                        MenuEvent.EventType.UPDATED,
                        menuItem.getId(),
                        menuItem.getRestaurant().getId(),
                        menuItem)
        );
    }

    public void publishMenuItemDeletedEvent(UUID menuItemId, UUID restaurantId) {
        eventPublisher.publishEvent(
                new MenuEvent(this,
                        MenuEvent.EventType.DELETED,
                        menuItemId,
                        restaurantId,
                        null)
        );
    }
}