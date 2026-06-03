-- Author: Nguyen The Anh

CREATE TABLE IF NOT EXISTS order_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_code VARCHAR(255),
    customer_order_code VARCHAR(255),
    order_status VARCHAR(255),
    description TEXT,
    post_office_code VARCHAR(255),
    post_office_id BIGINT,
    post_office_name VARCHAR(255),
    staff_code VARCHAR(255),
    staff_id BIGINT,
    staff_name VARCHAR(255),
    trip_id BIGINT,
    trip_code VARCHAR(255),
    vehicle_id BIGINT,
    vehicle_license_plate VARCHAR(255),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_label VARCHAR(500),
    event_time TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_order_history_order_tenant_event
    ON order_history (order_id, tenant_id, event_time DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_order_history_order_code
    ON order_history (order_code);
