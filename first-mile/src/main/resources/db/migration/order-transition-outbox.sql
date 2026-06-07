-- Author: Nguyen The Anh

CREATE TABLE IF NOT EXISTS order_transition_outbox (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    source VARCHAR(100) NOT NULL,
    request_payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT,
    next_retry_at TIMESTAMP WITHOUT TIME ZONE,
    processed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_order_transition_outbox_due
    ON order_transition_outbox (status, next_retry_at, id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_order_transition_outbox_tenant_key
    ON order_transition_outbox (tenant_id, idempotency_key);
