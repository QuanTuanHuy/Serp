-- Author: Nguyen The Anh

ALTER TABLE post_office_handover_manifest_orders
    DROP CONSTRAINT IF EXISTS fk_po_handover_manifest_orders_order;

ALTER TABLE post_office_handover_manifest_orders
    ADD COLUMN IF NOT EXISTS order_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS customer_order_code VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_known_status VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_po_handover_manifest_orders_order_code
    ON post_office_handover_manifest_orders (order_code);
