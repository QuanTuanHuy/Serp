CREATE TABLE IF NOT EXISTS delivery_manifest_orders (
    id                     BIGSERIAL PRIMARY KEY,
    tenant_id              BIGINT       NOT NULL,
    manifest_id            BIGINT       NOT NULL REFERENCES delivery_manifests(id),
    order_id               BIGINT       NOT NULL,
    order_code             VARCHAR(100) NOT NULL,
    sequence               INT          NOT NULL DEFAULT 0,
    delivery_attempt_count INT          NOT NULL DEFAULT 0,
    status                 VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    receiver_name          VARCHAR(255),
    receiver_phone         VARCHAR(50),
    receiver_address_detail TEXT,
    receiver_ward_code     VARCHAR(50),
    receiver_province_code VARCHAR(50),
    receiver_lat           DOUBLE PRECISION,
    receiver_lng           DOUBLE PRECISION,
    cod_amount             BIGINT       NOT NULL DEFAULT 0,
    cod_collected          BIGINT       NOT NULL DEFAULT 0,
    shipping_fee           BIGINT       NOT NULL DEFAULT 0,
    shipping_fee_collected BIGINT       NOT NULL DEFAULT 0,
    fee_payer              VARCHAR(20),
    delivery_payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    delivery_payment_amount BIGINT      NOT NULL DEFAULT 0,
    delivery_payment_app_trans_id VARCHAR(100),
    delivery_payment_confirmed_at TIMESTAMP,
    proof_photo_url        TEXT,
    failure_reason         VARCHAR(255),
    delivered_at           TIMESTAMP,
    note                   TEXT,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    created_by             VARCHAR(255),
    updated_by             VARCHAR(255)
);

ALTER TABLE delivery_manifest_orders
    ADD COLUMN IF NOT EXISTS delivery_payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN IF NOT EXISTS delivery_payment_amount BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_payment_app_trans_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS delivery_payment_confirmed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_dmo_manifest_id ON delivery_manifest_orders (manifest_id);
CREATE INDEX IF NOT EXISTS idx_dmo_order_code   ON delivery_manifest_orders (tenant_id, order_code);
CREATE INDEX IF NOT EXISTS idx_dmo_status       ON delivery_manifest_orders (tenant_id, status);
