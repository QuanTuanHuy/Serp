-- Author: Nguyen The Anh
-- Description: Part of Serp Project
-- Purpose: Configure delivery service catalog for billing formulas.

CREATE TABLE delivery_service_configs (
    id BIGSERIAL PRIMARY KEY,
    service_code VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    tenant_id BIGINT,
    CONSTRAINT uq_delivery_service_configs_service UNIQUE (service_code)
);

COMMENT ON TABLE delivery_service_configs IS 'Delivery service catalog configurable by TMS admin';

INSERT INTO delivery_service_configs (
    service_code,
    name,
    description,
    active,
    sort_order,
    created_at,
    updated_at
)
VALUES
    ('TIEU_CHUAN', 'Tiêu chuẩn', 'Dịch vụ giao hàng tiêu chuẩn, tối ưu chi phí.', TRUE, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('HOA_TOC', 'Hỏa tốc', 'Dịch vụ giao hàng nhanh, dùng cấu hình giá riêng.', FALSE, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (service_code) DO NOTHING;
