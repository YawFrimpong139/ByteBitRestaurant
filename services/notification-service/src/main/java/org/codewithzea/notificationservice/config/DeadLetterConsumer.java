package org.codewithzea.notificationservice.config;


import org.codewithzea.notificationservice.dto.NotificationMessage;
import org.codewithzea.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "dead.letter.queue")
    public void handleDeadLetter(NotificationMessage message) {
        log.error("Processing dead letter message: {}", message.getId());
        try {
            notificationService.markAsFailed(
                    message.getId(),
                    "Max retries exceeded - message moved to DLQ"
            );
            log.warn("Notification {} permanently failed after retries", message.getId());
        } catch (Exception e) {
            log.error("Failed to process dead letter message {}: {}", message.getId(), e.getMessage());
        }
    }
}
