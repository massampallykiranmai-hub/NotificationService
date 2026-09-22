package com.assignment.notifications.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempts")
public class DeliveryAttempt {

    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(name = "notification_id", nullable = false, length = 36)
    private String notificationId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 20)
    private AttemptOutcome outcome;

    @Column(name = "provider_message", length = 500)
    private String providerMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected DeliveryAttempt() {
        // JPA
    }

    public DeliveryAttempt(String notificationId, int attemptNumber, AttemptOutcome outcome, String providerMessage) {
        this.notificationId = notificationId;
        this.attemptNumber = attemptNumber;
        this.outcome = outcome;
        this.providerMessage = providerMessage;
    }

    public String getId() { return id; }
    public String getNotificationId() { return notificationId; }
    public int getAttemptNumber() { return attemptNumber; }
    public AttemptOutcome getOutcome() { return outcome; }
    public String getProviderMessage() { return providerMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
