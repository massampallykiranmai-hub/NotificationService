package com.assignment.notifications.delivery;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackoffCalculatorTest {

    @Test
    void doublesEachAttempt() {
        assertEquals(Duration.ofSeconds(5), BackoffCalculator.nextDelay(1, 5));
        assertEquals(Duration.ofSeconds(10), BackoffCalculator.nextDelay(2, 5));
        assertEquals(Duration.ofSeconds(20), BackoffCalculator.nextDelay(3, 5));
        assertEquals(Duration.ofSeconds(40), BackoffCalculator.nextDelay(4, 5));
    }

    @Test
    void isCappedSoItNeverWaitsForever() {
        Duration delay = BackoffCalculator.nextDelay(30, 60);
        assertEquals(Duration.ofSeconds(600), delay);
    }

    @Test
    void rejectsAttemptNumberBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> BackoffCalculator.nextDelay(0, 5));
    }
}
