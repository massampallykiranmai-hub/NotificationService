package com.assignment.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DeliveryWorker {

    private static final Logger log = LoggerFactory.getLogger(DeliveryWorker.class);

    private final NotificationService notificationService;

    public DeliveryWorker(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${delivery.poll-interval-ms}")
    public void poll() {
        int processed = notificationService.processDueBatch();
        if (processed > 0) {
            log.debug("delivery worker processed {} notification(s)", processed);
        }
    }
}
