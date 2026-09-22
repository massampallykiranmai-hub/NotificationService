package com.assignment.notifications.api;

import com.assignment.notifications.api.Dtos.*;
import com.assignment.notifications.domain.DeliveryAttempt;
import com.assignment.notifications.domain.Notification;
import com.assignment.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Accepts a notification for delivery. Pass an Idempotency-Key header to
     * make retries after a client-side timeout safe - a repeated key with an
     * identical body returns the original notification (200) instead of
     * queuing a second send; a repeated key with a different body is a
     * client bug and gets 409.
     *
     * 202 vs 200 is deliberate: 202 means "newly queued", 200 means
     * "this was your retry, nothing new happened".
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        var result = notificationService.intake(idempotencyKey, request);
        NotificationResponse body = toResponse(result.notification());

        if (result.wasExisting()) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/notifications/" + result.notification().getId()))
                .body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(toResponse(notificationService.get(id)));
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<AttemptHistoryResponse> attempts(@PathVariable String id) {
        List<DeliveryAttempt> attempts = notificationService.attempts(id);
        List<AttemptResponse> body = attempts.stream()
                .map(a -> new AttemptResponse(a.getId(), a.getAttemptNumber(), a.getOutcome(),
                        a.getProviderMessage(), a.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(new AttemptHistoryResponse(id, body));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getChannel(), n.getRecipient(), n.getSubject(),
                n.getStatus(), n.getAttemptCount(), n.getNextAttemptAt(),
                n.getCreatedAt(), n.getUpdatedAt()
        );
    }
}
