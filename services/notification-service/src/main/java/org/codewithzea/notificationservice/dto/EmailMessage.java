package org.codewithzea.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {
    private String to;
    private String subject;
    private String body;
    private boolean isHtml;
}
