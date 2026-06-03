-- Author: Nguyen The Anh

ALTER TABLE bag_orders
    DROP CONSTRAINT IF EXISTS fk_bag_orders_order;

ALTER TABLE bag_orders
    ADD COLUMN IF NOT EXISTS order_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS customer_order_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_known_status VARCHAR(255),
    ADD COLUMN IF NOT EXISTS origin_post_office_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS destination_post_office_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS total_weight_snapshot DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS total_volume_snapshot DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_bag_orders_tenant_order_id
    ON bag_orders (tenant_id, order_id);

CREATE INDEX IF NOT EXISTS idx_bag_orders_tenant_order_code_lower
    ON bag_orders (tenant_id, lower(order_code));

CREATE UNIQUE INDEX IF NOT EXISTS uq_bag_orders_bag_order_code_lower
    ON bag_orders (bag_id, lower(order_code))
    WHERE order_code IS NOT NULL;

ALTER TABLE handover_manifest_orders
    DROP CONSTRAINT IF EXISTS fk_handover_manifest_orders_order;

ALTER TABLE handover_manifest_orders
    ADD COLUMN IF NOT EXISTS order_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS customer_order_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_known_status VARCHAR(255),
    ADD COLUMN IF NOT EXISTS origin_post_office_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS destination_post_office_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS total_weight_snapshot DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS total_volume_snapshot DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_tenant_order_id
    ON handover_manifest_orders (tenant_id, order_id);

CREATE INDEX IF NOT EXISTS idx_handover_manifest_orders_tenant_order_code_lower
    ON handover_manifest_orders (tenant_id, lower(order_code));

CREATE UNIQUE INDEX IF NOT EXISTS uq_handover_manifest_orders_manifest_order_code_lower
    ON handover_manifest_orders (manifest_id, lower(order_code))
    WHERE order_code IS NOT NULL;

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
