package com.assignment.notifications.delivery;

import java.time.Duration;

/**
 * Kept as a separate helper so the retry timing can be tested in isolation,
 * without any database or delivery infrastructure involved. This keeps the
 * backoff logic simple, predictable, and easy to verify.
 */
public final class BackoffCalculator {

    private static final long MAX_BACKOFF_SECONDS = 600; // 10 min ceiling

    private BackoffCalculator() {}


     // Calculates the next delay for a retry attempt based on exponential backoff.
    public static Duration nextDelay(int attemptNumber, int baseSeconds) {
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be >= 1");
        }
        long raw = (long) baseSeconds * (1L << Math.min(attemptNumber - 1, 20));
        return Duration.ofSeconds(Math.min(raw, MAX_BACKOFF_SECONDS));
    }
}
