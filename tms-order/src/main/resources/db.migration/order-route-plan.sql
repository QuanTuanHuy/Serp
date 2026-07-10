-- Author: Nguyen The Anh
-- Description: Store planned transport route and current hub for TMS orders

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS planned_route JSONB,
    ADD COLUMN IF NOT EXISTS current_hub_id BIGINT,
    ADD COLUMN IF NOT EXISTS current_hub_code VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_orders_current_hub_id
    ON orders (tenant_id, current_hub_id);
