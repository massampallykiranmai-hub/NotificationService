package com.assignment.notifications.delivery;

import com.assignment.notifications.domain.AttemptOutcome;

public record ProviderResult(AttemptOutcome outcome, String message) {
    public static ProviderResult success(String message)
    {

        return new ProviderResult(AttemptOutcome.SUCCESS, message);
    }
    public static ProviderResult retryable(String message) {
        return new ProviderResult(AttemptOutcome.RETRYABLE_ERROR, message);
    }
    public static ProviderResult permanent(String message) {
        return new ProviderResult(AttemptOutcome.PERMANENT_ERROR, message);
    }
}
