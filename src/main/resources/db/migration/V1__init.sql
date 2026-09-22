CREATE TABLE notifications (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,
    channel             VARCHAR(20)  NOT NULL,
    recipient           VARCHAR(255) NOT NULL,
    subject             VARCHAR(255) NULL,
    body                TEXT         NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    attempt_count       INT          NOT NULL DEFAULT 0,
    next_attempt_at     DATETIME(3)  NOT NULL,
    idempotency_key     VARCHAR(255) NULL,
    request_fingerprint VARCHAR(64) NULL,
    created_at          DATETIME(3)  NOT NULL,
    updated_at          DATETIME(3)  NOT NULL,
    CONSTRAINT uq_notifications_idempotency_key UNIQUE (idempotency_key)
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_claim
    ON notifications (status, next_attempt_at);

CREATE TABLE delivery_attempts (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    notification_id  VARCHAR(36)  NOT NULL,
    attempt_number   INT          NOT NULL,
    outcome          VARCHAR(20)  NOT NULL,
    provider_message VARCHAR(500) NULL,
    created_at       DATETIME(3)  NOT NULL,

    CONSTRAINT fk_delivery_attempts_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (id)
) ENGINE=InnoDB;

CREATE INDEX idx_delivery_attempts_notification
    ON delivery_attempts (notification_id, attempt_number);