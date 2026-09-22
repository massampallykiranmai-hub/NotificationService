package com.assignment.notifications.repo;

import com.assignment.notifications.domain.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, String> {
    List<DeliveryAttempt> findByNotificationIdOrderByAttemptNumberAsc(String notificationId);
}
