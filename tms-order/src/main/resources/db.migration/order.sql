-- Author: Nguyen The Anh
-- CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(255),
    customer_order_code VARCHAR(255),
    sender_name VARCHAR(255),
    sender_phone VARCHAR(255),
    sender_ward_code VARCHAR(255),
    sender_province_code VARCHAR(255),
    sender_address_detail VARCHAR(255),
    sender_location geography(Point, 4326),
    pickup_time_start TIMESTAMP,
    pickup_time_end TIMESTAMP,
    delivery_request_time VARCHAR(255),
    pickup_method VARCHAR(50) DEFAULT 'COURIER_PICKUP',
    receiver_name VARCHAR(255),
    receiver_phone VARCHAR(255),
    receiver_ward_code VARCHAR(255),
    receiver_province_code VARCHAR(255),
    receiver_address_detail VARCHAR(255),
    receiver_location geography(Point, 4326),
    origin_post_office_code VARCHAR(255),
    destination_post_office_code VARCHAR(255),
    status VARCHAR(255),
    is_confirm BOOLEAN NOT NULL DEFAULT FALSE,
    total_weight DOUBLE PRECISION,
    total_value DOUBLE PRECISION,
    dimensions jsonb,
    total_volume DOUBLE PRECISION,
    pickup_attempts INT DEFAULT 0,
    order_product_category VARCHAR(255),
    order_type VARCHAR(255),
    base_shipping_fee BIGINT,
    cod_fee BIGINT,
    extra_fee BIGINT,
    total_shipping_fee BIGINT,
    cod_amount BIGINT,
    fee_payer VARCHAR(255),
    payment_status VARCHAR(255),
    note TEXT,
    cancelled_at TIMESTAMP DEFAULT NULL,
    cancel_reason TEXT DEFAULT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_orders_order_code ON orders(order_code);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_tenant_id ON orders(tenant_id);
CREATE INDEX IF NOT EXISTS idx_orders_sender_location ON orders USING GIST(sender_location);
CREATE INDEX IF NOT EXISTS idx_orders_receiver_location ON orders USING GIST(receiver_location);
CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_order_code_unique
    ON orders(order_code)
    WHERE order_code IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_tenant_customer_order_code_unique
    ON orders(tenant_id, lower(customer_order_code))
    WHERE tenant_id IS NOT NULL AND customer_order_code IS NOT NULL;
