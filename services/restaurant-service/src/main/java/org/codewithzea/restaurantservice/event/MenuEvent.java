package org.codewithzea.restaurantservice.event;


import org.codewithzea.restaurantservice.model.MenuItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class MenuEvent extends ApplicationEvent {
    private final EventType eventType;
    private final UUID menuItemId;
    private final UUID restaurantId;
    private final MenuItem menuItem;

    public MenuEvent(Object source, EventType eventType, UUID menuItemId, UUID restaurantId, MenuItem menuItem) {
        super(source);
        this.eventType = eventType;
        this.menuItemId = menuItemId;
        this.restaurantId = restaurantId;
        this.menuItem = menuItem;
    }

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
}
