package org.codewithzea.restaurantservice.exception;



public class MenuOptimisticLockException extends RuntimeException {
    public MenuOptimisticLockException(String message) {
        super("Concurrent modification detected: " + message);
    }
}