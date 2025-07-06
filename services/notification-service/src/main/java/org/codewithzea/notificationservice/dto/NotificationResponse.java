package org.codewithzea.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private boolean success;
    private String message;
    private String providerResponse;
}
