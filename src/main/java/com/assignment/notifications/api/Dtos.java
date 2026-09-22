package com.assignment.notifications.api;

import com.assignment.notifications.domain.AttemptOutcome;
import com.assignment.notifications.domain.Channel;
import com.assignment.notifications.domain.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record CreateNotificationRequest(
            @NotNull Channel channel,
            @NotBlank String recipient,
            String subject,
            @NotBlank String body
    ) {}

    public record NotificationResponse(
            String id,
            Channel channel,
            String recipient,
            String subject,
             NotificationStatus status,
            int attemptCount,
            Instant nextAttemptAt,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record AttemptResponse(
            String id,
            int attemptNumber,
            AttemptOutcome outcome,
            String providerMessage,
            Instant createdAt
    ) {}

    public record AttemptHistoryResponse(
            String notificationId,
            List<AttemptResponse> attempts
    ) {}

    public record ErrorResponse(
            String type,
            String title,
            int status,
            String detail
    ) {}
}
