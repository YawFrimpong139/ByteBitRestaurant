package org.codewithzea.notificationservice.service;

import org.codewithzea.notificationservice.dto.SmsMessage;
import org.codewithzea.notificationservice.exception.NotificationProcessingException;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    public void send(SmsMessage sms) {
        // In real implementation, connect to SMS provider
        System.out.println("Sending SMS to: " + sms.getTo());
        System.out.println("Message: " + sms.getMessage());

        // Simulate occasional failures
        if (sms.getTo().contains("fail")) {
            throw new NotificationProcessingException("Failed to send SMS to " + sms.getTo());
        }
    }
}

