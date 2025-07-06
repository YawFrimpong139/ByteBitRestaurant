package org.codewithzea.notificationservice.dto;

import org.codewithzea.notificationservice.model.NotificationType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private UUID id;
    private String recipient;
    private NotificationType type;
    private String templateId;
    private Map<String, String> variables;
    private int retryCount;
}
