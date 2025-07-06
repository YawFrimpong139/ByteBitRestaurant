package org.codewithzea.notificationservice.service;

import org.codewithzea.notificationservice.exception.ProviderUnavailableException;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ProviderService {
    private final Random random = new Random();

    public String send(String recipient, String message, String subject) {
        // Simulate provider availability (10% chance of being unavailable)
        if (random.nextInt(10) == 0) {
            throw new ProviderUnavailableException("Notification provider is currently unavailable");
        }

        // Simulate sending (in real app, this would call Email/SMS provider API)
        System.out.println("Sending notification to: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);

        // Simulate provider response
        return "Notification queued successfully";
    }
}
