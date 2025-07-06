package org.codewithzea.notificationservice.exception;

public class NotificationFailedException extends RuntimeException {
    public NotificationFailedException(String message) {
        super(message);
    }
}