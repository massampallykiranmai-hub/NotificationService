package com.assignment.notifications.domain;

public enum NotificationStatus {
    PENDING,    // accepted, not yet attempted (or due for its next retry)
    RETRYING,   // had a transient failure, waiting for next_attempt_at
    DELIVERED,  // terminal: provider accepted it
    FAILED      // terminal: permanent error, or retries exhausted
}
