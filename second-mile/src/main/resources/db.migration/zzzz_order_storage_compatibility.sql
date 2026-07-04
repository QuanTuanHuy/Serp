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
