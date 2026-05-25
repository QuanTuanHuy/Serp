/*
Author: Nguyen The Anh
Description: Part of Serp Project - Table Vehicles
*/

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    max_weight DOUBLE PRECISION DEFAULT 0,
    max_volume DOUBLE PRECISION DEFAULT 0,
    max_bags INTEGER DEFAULT 0,
    image_url TEXT,
    hub_id BIGINT,
    assigned_staff_id BIGINT,
    status VARCHAR(50) DEFAULT 'ACTIVE',

    -- Fields from AbstractAudit (MappedSuperclass)
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Unique license plate per tenant (case-insensitive)
CREATE UNIQUE INDEX IF NOT EXISTS uq_vehicles_tenant_license_plate_lower
    ON vehicles (tenant_id, lower(license_plate));

CREATE INDEX IF NOT EXISTS idx_vehicles_hub_id ON vehicles(hub_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_assigned_staff_id ON vehicles(assigned_staff_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_status ON vehicles(status);

COMMENT ON TABLE vehicles IS 'Bảng lưu trữ thông tin phương tiện trong dự án Serp Project';

