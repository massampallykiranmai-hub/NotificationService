package com.assignment.notifications.service;

import com.assignment.notifications.api.Dtos.CreateNotificationRequest;
import com.assignment.notifications.api.IdempotencyConflictException;
import com.assignment.notifications.api.NotificationNotFoundException;
import com.assignment.notifications.config.DeliveryProperties;
import com.assignment.notifications.delivery.BackoffCalculator;
import com.assignment.notifications.delivery.ChannelProvider;
import com.assignment.notifications.delivery.ProviderResult;
import com.assignment.notifications.domain.*;
import com.assignment.notifications.repo.DeliveryAttemptRepository;
import com.assignment.notifications.repo.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final DeliveryAttemptRepository attemptRepository;
    private final DeliveryProperties properties;
    private final Map<Channel, ChannelProvider> providersByChannel;

    public NotificationService(NotificationRepository notificationRepository,
                                DeliveryAttemptRepository attemptRepository,
                                DeliveryProperties properties,
                                List<ChannelProvider> providers) {
        this.notificationRepository = notificationRepository;
        this.attemptRepository = attemptRepository;
        this.properties = properties;
        // One interface, one bean per channel - this map is the whole
        // "registry". Adding a channel is a new @Component, not a new
        // branch here.
        this.providersByChannel = providers.stream()
                .collect(Collectors.toMap(ChannelProvider::channel, Function.identity()));
    }

    // ---- Intake -----------------------------------------------------

    /**
     * Creates a notification, or - if idempotencyKey has been seen before
     * with an identical request - returns the original instead of creating
     * a duplicate. The uniqueness check happens at the database via a
     * unique index, not a read-then-write check in application code, so two
     * concurrent retries of the same request can't both slip through.
     */
    @Transactional
    public IntakeResult intake(String idempotencyKey, CreateNotificationRequest request) {
        String fingerprint = fingerprint(request);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = notificationRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                Notification n = existing.get();
                if (!fingerprint.equals(n.getRequestFingerprint())) {
                    throw new IdempotencyConflictException(idempotencyKey);
                }
                return new IntakeResult(n, true);
            }
        }

        Notification notification = new Notification(
                request.channel(), request.recipient(), request.subject(), request.body(),
                idempotencyKey, fingerprint
        );

        try {
            notification = notificationRepository.save(notification);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Race: two concurrent requests with the same key both missed
            // the check above. The DB's unique index is the real guard;
            // the loser here just re-reads and replays like a normal cache hit.
            Notification winner = notificationRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> e);
            if (!fingerprint.equals(winner.getRequestFingerprint())) {
                throw new IdempotencyConflictException(idempotencyKey);
            }
            return new IntakeResult(winner, true);
        }

        return new IntakeResult(notification, false);
    }

    @Transactional(readOnly = true)
    public Notification get(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<DeliveryAttempt> attempts(String id) {
        get(id); // 404s if the notification doesn't exist
        return attemptRepository.findByNotificationIdOrderByAttemptNumberAsc(id);
    }

    // ---- Delivery (called by the scheduled worker) -------------------

    /**
     * Claims a batch of due rows and attempts delivery for each, all in one
     * transaction: the claim's row locks are what the batch's updates rely
     * on, and releasing them only at commit is what stops two worker
     * instances (or two ticks of the same worker) from grabbing the same
     * row. Trade-off: a crash mid-batch rolls the whole batch back rather
     * than just the row being processed - acceptable at batch-size 10, see
     * README for where I'd revisit this.
     */
    @Transactional
    public int processDueBatch() {
        List<String> dueIds = notificationRepository.claimDueIds(Instant.now(), properties.getBatchSize());
        for (String id : dueIds) {
            processOne(notificationRepository.findById(id).orElseThrow());
        }
        return dueIds.size();
    }

    private void processOne(Notification notification) {
        ChannelProvider provider = providersByChannel.get(notification.getChannel());
        ProviderResult result = provider.send(notification);
        int attemptNumber = notification.getAttemptCount() + 1;

        attemptRepository.save(new DeliveryAttempt(
                notification.getId(), attemptNumber, result.outcome(), result.message()
        ));
        notification.setAttemptCount(attemptNumber);

        switch (result.outcome()) {
            case SUCCESS -> notification.setStatus(NotificationStatus.DELIVERED);
            case PERMANENT_ERROR -> notification.setStatus(NotificationStatus.FAILED);
            case RETRYABLE_ERROR -> {
                if (attemptNumber >= properties.getMaxAttempts()) {
                    notification.setStatus(NotificationStatus.FAILED);
                } else {
                    Duration delay = BackoffCalculator.nextDelay(attemptNumber, properties.getBackoffBaseSeconds());
                    notification.setNextAttemptAt(Instant.now().plus(delay));
                    notification.setStatus(NotificationStatus.RETRYING);
                }
            }
        }

        notificationRepository.save(notification);
        log.info("notification={} attempt={} outcome={} newStatus={}",
                notification.getId(), attemptNumber, result.outcome(), notification.getStatus());
    }

    private String fingerprint(CreateNotificationRequest request) {
        String raw = request.channel() + "|" + request.recipient() + "|" +
                request.subject() + "|" + request.body();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record IntakeResult(Notification notification, boolean wasExisting) {}
}
