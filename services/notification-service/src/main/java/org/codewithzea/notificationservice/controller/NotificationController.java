package org.codewithzea.notificationservice.controller;

import org.codewithzea.notificationservice.dto.NotificationMessage;
import org.codewithzea.notificationservice.service.NotificationDispatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationDispatcher dispatcher;

    public NotificationController(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public void sendNotification(@RequestBody NotificationMessage message) {
        dispatcher.dispatch(message);
    }
}