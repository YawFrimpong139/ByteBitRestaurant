package org.codewithzea.notificationservice.config;


import org.codewithzea.notificationservice.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static org.codewithzea.notificationservice.config.RabbitMQConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailNotification(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "notification.email",
                message
        );
        log.info("Sent email notification to queue: {}", message.getId());
    }

    public void sendSmsNotification(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "notification.sms",
                message
        );
        log.info("Sent SMS notification to queue: {}", message.getId());
    }

    public void sendPushNotification(NotificationMessage message) {
        rabbitTemplate.convertAndSend(
                "notification.exchange",
                "notification.push",
                message
        );
        log.info("Sent push notification to queue: {}", message.getId());
    }
}
