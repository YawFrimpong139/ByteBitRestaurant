package org.codewithzea.notificationservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PushMessage {
    private String deviceToken;
    private String title;
    private String body;
    private Map<String, String> data;
}
