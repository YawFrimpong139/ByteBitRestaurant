package org.codewithzea.notificationservice.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecord {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String recipient;
    private String templateId;
    private String notificationType;
    private String errorMessage;
    private Integer retryCount;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
