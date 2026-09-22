package com.assignment.notifications.service;

import com.assignment.notifications.api.Dtos.CreateNotificationRequest;
import com.assignment.notifications.api.IdempotencyConflictException;
import com.assignment.notifications.domain.Channel;
import com.assignment.notifications.domain.Notification;
import com.assignment.notifications.domain.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These are the two behaviours worth proving against a real database rather
 * than trusting the SQL by eye: idempotent intake, and the worker actually
 * moving a notification through PENDING -> DELIVERED/RETRYING/FAILED.
 */
@Testcontainers
@SpringBootTest
class NotificationServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("notifications")
            .withUsername("notif")
            .withPassword("notif");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired NotificationService notificationService;

    @Test
    void retryingWithTheSameIdempotencyKeyReturnsTheOriginal() {
        var request = new CreateNotificationRequest(Channel.EMAIL, "ada@example.com", "hi", "body");

        var first = notificationService.intake("key-1", request);
        var second = notificationService.intake("key-1", request);

        assertFalse(first.wasExisting());
        assertTrue(second.wasExisting());
        assertEquals(first.notification().getId(), second.notification().getId());
    }

    @Test
    void sameKeyDifferentBodyIsRejected() {
        notificationService.intake("key-2",
                new CreateNotificationRequest(Channel.EMAIL, "ada@example.com", "hi", "body"));

        assertThrows(IdempotencyConflictException.class, () ->
                notificationService.intake("key-2",
                        new CreateNotificationRequest(Channel.EMAIL, "ada@example.com", "hi", "a different body")));
    }

    @Test
    void invalidRecipientEndsUpFailedNotRetrying() {
        var result = notificationService.intake(null,
                new CreateNotificationRequest(Channel.EMAIL, "not-an-email", "hi", "body"));

        notificationService.processDueBatch();

        Notification updated = notificationService.get(result.notification().getId());
        assertEquals(NotificationStatus.FAILED, updated.getStatus());
        assertEquals(1, updated.getAttemptCount());
    }
}
