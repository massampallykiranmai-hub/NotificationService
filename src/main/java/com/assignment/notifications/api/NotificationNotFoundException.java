package com.assignment.notifications.api;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String id) {
        super("No notification found with id '" + id + "'");
    }
}
