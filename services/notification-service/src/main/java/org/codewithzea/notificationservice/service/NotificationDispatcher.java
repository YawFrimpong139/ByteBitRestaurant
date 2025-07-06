package org.codewithzea.notificationservice.service;

import org.codewithzea.notificationservice.dto.*;
import org.codewithzea.notificationservice.exception.*;
import org.codewithzea.notificationservice.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final EmailService emailService;
    private final SmsService smsService;
    private final PushService pushService;
    private final TemplateService templateService;

    public void dispatch(NotificationMessage message) {
        try {
            switch (message.getType()) {
                case EMAIL -> {
                    EmailMessage email = prepareEmail(message);
                    emailService.send(email);
                    log.info("Email notification sent to {}", message.getRecipient());
                }
                case SMS -> {
                    SmsMessage sms = prepareSms(message);
                    smsService.send(sms);
                    log.info("SMS notification sent to {}", message.getRecipient());
                }
                case PUSH -> {
                    PushMessage push = preparePush(message);
                    pushService.send(push);
                    log.info("Push notification sent to {}", message.getRecipient());
                }
                default -> throw new NotificationProcessingException(
                        "Unsupported notification type: " + message.getType()
                );
            }
        } catch (TemplateNotFoundException e) {
            log.error("Template error for notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            throw new NotificationProcessingException("Failed to process notification: " + e.getMessage());
        }
    }

    private EmailMessage prepareEmail(NotificationMessage message) {
        Optional<String> templateContent = templateService.template(message.getTemplateId());
        String content = templateContent.orElseThrow(() ->
                new TemplateNotFoundException(message.getTemplateId())
        );

        String body = templateService.processTemplate(content, message.getVariables());

        return EmailMessage.builder()
                .to(message.getRecipient())
                .subject(getSubjectFromTemplate(message))
                .body(body)
                .isHtml(true)
                .build();
    }

    @Retryable(value = RetryableException.class, maxAttempts = 3)
    private SmsMessage prepareSms(NotificationMessage message) {
        Template template = templateService.getTemplate(message.getTemplateId());
        if (template == null) {
            throw new RetryableException("Template not available, will retry");
        }

        String content = templateService.processTemplate(template.getContent(), message.getVariables());

        return SmsMessage.builder()
                .to(message.getRecipient())
                .message(content)
                .build();
    }

    private PushMessage preparePush(NotificationMessage message) {
        Template template = templateService.getTemplate(message.getTemplateId());
        if (template == null) {
            throw new TemplateNotFoundException(message.getTemplateId());
        }

        String content = templateService.processTemplate(template.getContent(), message.getVariables());

        return PushMessage.builder()
                .deviceToken(message.getRecipient())
                .title(template.getName())
                .body(content)
                .data(message.getVariables())
                .build();
    }

    private String getSubjectFromTemplate(NotificationMessage message) {
        Template template = templateService.getTemplate(message.getTemplateId());
        return template != null ? template.getName() : "Notification";
    }
}