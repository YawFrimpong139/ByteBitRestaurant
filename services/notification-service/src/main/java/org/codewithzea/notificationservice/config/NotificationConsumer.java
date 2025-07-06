package org.codewithzea.notificationservice.config;


import org.codewithzea.notificationservice.dto.NotificationMessage;
import org.codewithzea.notificationservice.exception.NotificationProcessingException;
import org.codewithzea.notificationservice.exception.RetryableException;
import org.codewithzea.notificationservice.service.NotificationDispatcher;
import org.codewithzea.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationDispatcher dispatcher;
    private final NotificationService notificationService;

    @RabbitListener(queues = "email.queue")
    public void handleEmailNotification(NotificationMessage message) {
        processNotification(message);
    }

    @RabbitListener(queues = "sms.queue")
    public void handleSmsNotification(NotificationMessage message) {
        processNotification(message);
    }

    @RabbitListener(queues = "push.queue")
    public void handlePushNotification(NotificationMessage message) {
        processNotification(message);
    }

    @Retryable(
            value = RetryableException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void processNotification(NotificationMessage message) {
        try {
            log.info("Processing notification: {}", message.getId());
            dispatcher.dispatch(message);
            notificationService.markAsProcessed(message.getId());
        } catch (RetryableException e) {
            log.warn("Retryable error processing notification {}: {}", message.getId(), e.getMessage());
            throw e;
        } catch (NotificationProcessingException e) {
            log.error("Permanent error processing notification {}: {}", message.getId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Permanent failure");
        }
    }
}
