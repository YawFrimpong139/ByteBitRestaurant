package org.codewithzea.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codewithzea.notificationservice.dto.NotificationMessage;
import org.codewithzea.notificationservice.dto.NotificationResponse;
import org.codewithzea.notificationservice.exception.NotificationFailedException;
import org.codewithzea.notificationservice.exception.ProviderUnavailableException;
import org.codewithzea.notificationservice.exception.TemplateNotFoundException;
import org.codewithzea.notificationservice.model.NotificationRecord;
import org.codewithzea.notificationservice.model.NotificationStatus;
import org.codewithzea.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final TemplateService templateService;
    private final ProviderService providerService;
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationRecord createNotification(NotificationMessage message) {
        NotificationRecord record = NotificationRecord.builder()
                .id(message.getId().toString())
                .recipient(message.getRecipient())
                .templateId(message.getTemplateId())
                .notificationType(message.getType().name())
                .status(NotificationStatus.PENDING)
                .retryCount(message.getRetryCount())  // Now this will work
                .build();

        return notificationRepository.save(record);
    }

    @Transactional
    public NotificationResponse processNotification(NotificationMessage message) {
        try {
            // Update status to PROCESSING
            updateStatus(message.getId(), NotificationStatus.PROCESSING);

            // Get template and render message
            String messageContent = templateService.processTemplate(
                    templateService.getTemplate(message.getTemplateId()).getContent(),
                    message.getVariables()
            );

            // Send via provider (implementation depends on your provider)
            String providerResponse = providerService.send(
                    message.getRecipient(),
                    messageContent,
                    getSubjectFromTemplate(message.getTemplateId())
            );

            // Mark as completed
            updateStatus(message.getId(), NotificationStatus.COMPLETED);

            return new NotificationResponse(
                    true,
                    "Notification sent successfully",
                    providerResponse
            );
        } catch (TemplateNotFoundException | ProviderUnavailableException e) {
            handleFailure(message, e.getMessage());
            throw e;
        } catch (Exception e) {
            handleFailure(message, e.getMessage());
            throw new NotificationFailedException("Failed to send notification: " + e.getMessage());
        }
    }

    @Transactional
    public void markAsProcessed(UUID notificationId) {
        notificationRepository.findById(notificationId.toString()).ifPresent(record -> {
            record.setStatus(NotificationStatus.COMPLETED);
            notificationRepository.save(record);
            log.info("Notification {} marked as processed", notificationId);
        });
    }

    @Transactional
    public void markForRetry(UUID notificationId, String error) {
        notificationRepository.findById(notificationId.toString()).ifPresent(record -> {
            record.setStatus(NotificationStatus.RETRYING);
            record.setErrorMessage(error);
            notificationRepository.save(record);
            log.warn("Notification {} marked for retry: {}", notificationId, error);
        });
    }

    private String getSubjectFromTemplate(String templateId) {
        // Implement logic to get subject from template if needed
        return "Notification"; // Default subject
    }

    @Transactional
    protected void updateStatus(UUID notificationId, NotificationStatus status) {
        notificationRepository.findById(notificationId.toString()).ifPresent(record -> {
            record.setStatus(status);
            notificationRepository.save(record);
            log.info("Notification {} status updated to {}", notificationId, status);
        });
    }

    // In NotificationService.java
    @Transactional
    public void markAsFailed(UUID notificationId, String errorMessage) {
        notificationRepository.findById(notificationId.toString()).ifPresent(record -> {
            record.setStatus(NotificationStatus.FAILED);
            record.setErrorMessage(errorMessage);
            notificationRepository.save(record);
            log.error("Notification {} marked as failed: {}", notificationId, errorMessage);
        });
    }

    @Transactional
    protected void handleFailure(NotificationMessage message, String error) {
        notificationRepository.findById(message.getId().toString()).ifPresent(record -> {
            if (message.getRetryCount() > 0) {
                record.setStatus(NotificationStatus.RETRYING);
                record.setRetryCount(message.getRetryCount() - 1);
                log.warn("Notification {} marked for retry. Attempts left: {}",
                        message.getId(), record.getRetryCount());
            } else {
                record.setStatus(NotificationStatus.FAILED);
                log.error("Notification {} marked as failed: {}", message.getId(), error);
            }
            record.setErrorMessage(error);
            notificationRepository.save(record);
        });
    }
}