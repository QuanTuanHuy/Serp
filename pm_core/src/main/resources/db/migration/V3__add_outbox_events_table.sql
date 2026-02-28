-- Author: QuanTuanHuy
-- Description: Part of Serp Project

CREATE TABLE outbox_events (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    BIGINT NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    topic           VARCHAR(255) NOT NULL,
    partition_key   VARCHAR(255),
    payload         JSONB NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count     INT NOT NULL DEFAULT 0,
    max_retries     INT NOT NULL DEFAULT 5,
    next_retry_at   TIMESTAMP,
    published_at    TIMESTAMP,
    error_message   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,

    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING','PUBLISHED','FAILED','DEAD'))
);

CREATE INDEX idx_outbox_events_pending ON outbox_events (status, next_retry_at)
    WHERE status IN ('PENDING', 'FAILED');
CREATE INDEX idx_outbox_events_cleanup ON outbox_events (published_at)
    WHERE status = 'PUBLISHED';
CREATE INDEX idx_outbox_events_aggregate ON outbox_events (tenant_id, aggregate_type, aggregate_id);
