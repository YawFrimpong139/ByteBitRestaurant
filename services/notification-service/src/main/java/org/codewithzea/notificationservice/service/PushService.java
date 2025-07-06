package org.codewithzea.notificationservice.service;

import org.codewithzea.notificationservice.dto.PushMessage;
import org.codewithzea.notificationservice.exception.NotificationProcessingException;
import org.springframework.stereotype.Service;

@Service
public class PushService {
    public void send(PushMessage push) {
        // In real implementation, connect to push notification service
        System.out.println("Sending push to device: " + push.getDeviceToken());
        System.out.println("Title: " + push.getTitle());
        System.out.println("Body: " + push.getBody());

        // Simulate occasional failures
        if (push.getDeviceToken().contains("fail")) {
            throw new NotificationProcessingException("Failed to send push to " + push.getDeviceToken());
        }
    }
}
