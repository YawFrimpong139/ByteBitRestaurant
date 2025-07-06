package org.codewithzea.notificationservice.exception;

public class NotificationProcessingException extends RuntimeException {
    public NotificationProcessingException(String message) {
        super(message);
    }
}