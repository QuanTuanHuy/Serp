CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,

                        order_code VARCHAR(255) NOT NULL,
                        customer_order_code VARCHAR(255),

                        origin_post_office_code VARCHAR(255),
                        destination_post_office_code VARCHAR(255),

                        status VARCHAR(50),
                        order_product_category VARCHAR(100),
                        order_type VARCHAR(50),

                        total_weight DOUBLE PRECISION,
                        dimensions JSONB,
                        total_volume DOUBLE PRECISION,

                        note TEXT,

                        tenant_id BIGINT,
                        created_by VARCHAR(255),
                        created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_by VARCHAR(255),
                        updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Ràng buộc duy nhất cho order_code
ALTER TABLE orders ADD CONSTRAINT uk_orders_order_code UNIQUE (order_code);

-- Các Index tối ưu truy vấn
CREATE INDEX idx_orders_order_code ON orders(order_code);
CREATE INDEX idx_orders_tenant_id ON orders(tenant_id);