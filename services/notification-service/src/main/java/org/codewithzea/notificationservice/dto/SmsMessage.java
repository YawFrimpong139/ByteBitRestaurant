package org.codewithzea.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsMessage {
    private String to;
    private String message;
}
