package org.codewithzea.notificationservice.repository;

import org.codewithzea.notificationservice.model.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationRecord, String> {
}