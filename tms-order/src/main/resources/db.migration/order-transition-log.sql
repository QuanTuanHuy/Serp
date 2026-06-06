-- Author: Nguyen The Anh

CREATE TABLE IF NOT EXISTS order_transition_log (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    source VARCHAR(100),
    request_payload TEXT,
    response_payload TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_order_transition_log_tenant_key UNIQUE (tenant_id, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_order_transition_log_tenant
    ON order_transition_log (tenant_id);
