package com.assignment.notifications.repo;

import com.assignment.notifications.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    /**
     * Claims up to {@code limit} rows that are due for an attempt, skipping
     * any row another worker/replica already has locked. This is what makes
     * it safe to run more than one instance of this service: two workers
     * polling at the same moment never grab the same notification.
     *
     * MySQL 8 supports SKIP LOCKED; without it a second worker would block
     * on the lock instead of just moving on to the next row.
     */
    @Query(value = """
            SELECT id FROM notifications
            WHERE status IN ('PENDING', 'RETRYING')
              AND next_attempt_at <= :now
            ORDER BY next_attempt_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<String> claimDueIds(@Param("now") java.time.Instant now, @Param("limit") int limit);
}
