-- Author: Nguyen The Anh

ALTER TABLE trip_order
    ADD COLUMN IF NOT EXISTS delivery_status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS delivery_attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS cod_collected BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shipping_fee_collected BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN IF NOT EXISTS delivery_payment_amount BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_payment_app_trans_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS delivery_payment_confirmed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(255),
    ADD COLUMN IF NOT EXISTS delivery_note TEXT,
    ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS returned_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_trip_order_delivery_status
    ON trip_order (tenant_id, delivery_status);

CREATE UNIQUE INDEX IF NOT EXISTS uq_checkin_delivery_tenant_trip_order
    ON checkin (tenant_id, trip_order_id)
    WHERE checkin_type = 'DELIVERY' AND trip_order_id IS NOT NULL;
