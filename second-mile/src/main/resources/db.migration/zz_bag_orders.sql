/*
Author: Nguyen The Anh
Description: Part of Serp Project - Table Bag Orders
*/

CREATE TABLE IF NOT EXISTS bag_orders (
    id BIGSERIAL PRIMARY KEY,
    bag_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_code VARCHAR(255),
    customer_order_code VARCHAR(255),
    last_known_status VARCHAR(255),
    origin_post_office_code VARCHAR(255),
    destination_post_office_code VARCHAR(255),
    total_weight_snapshot DOUBLE PRECISION,
    total_volume_snapshot DOUBLE PRECISION,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_bag_orders_bag_order UNIQUE (bag_id, order_id),
    CONSTRAINT fk_bag_orders_bag FOREIGN KEY (bag_id) REFERENCES bags(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_bag_orders_bag_id ON bag_orders(bag_id);
CREATE INDEX IF NOT EXISTS idx_bag_orders_order_id ON bag_orders(order_id);
CREATE INDEX IF NOT EXISTS idx_bag_orders_tenant_id ON bag_orders(tenant_id);
CREATE INDEX IF NOT EXISTS idx_bag_orders_tenant_order_id ON bag_orders(tenant_id, order_id);
CREATE INDEX IF NOT EXISTS idx_bag_orders_tenant_order_code_lower ON bag_orders(tenant_id, lower(order_code));

COMMENT ON TABLE bag_orders IS 'Bag order links using canonical tms-order references';
