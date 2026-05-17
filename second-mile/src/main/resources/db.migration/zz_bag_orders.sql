/*
Author: Nguyen The Anh
Description: Part of Serp Project - Table Bag Orders
*/

CREATE TABLE IF NOT EXISTS bag_orders (
    id BIGSERIAL PRIMARY KEY,
    bag_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_bag_orders_bag_order UNIQUE (bag_id, order_id),
    CONSTRAINT fk_bag_orders_bag FOREIGN KEY (bag_id) REFERENCES bags(id) ON DELETE CASCADE,
    CONSTRAINT fk_bag_orders_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_bag_orders_bag_id ON bag_orders(bag_id);
CREATE INDEX IF NOT EXISTS idx_bag_orders_order_id ON bag_orders(order_id);
CREATE INDEX IF NOT EXISTS idx_bag_orders_tenant_id ON bag_orders(tenant_id);

COMMENT ON TABLE bag_orders IS 'Bảng ánh xạ đơn hàng vào túi hàng';
