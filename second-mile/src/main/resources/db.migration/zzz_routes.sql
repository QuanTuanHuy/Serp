/*
Author: Nguyen The Anh
Description: Part of Serp Project - Table Routes
*/

CREATE TABLE IF NOT EXISTS routes (
    id BIGSERIAL PRIMARY KEY,
    route_code VARCHAR(100) NOT NULL,
    route_name VARCHAR(255) NOT NULL,
    origin_hub_id BIGINT NOT NULL,
    destination_type VARCHAR(30) NOT NULL,
    destination_hub_id BIGINT,
    destination_post_office_code VARCHAR(255),
    vehicle_id BIGINT,
    estimated_distance_km DOUBLE PRECISION,
    estimated_duration_minutes INTEGER,
    fixed_departure_time TIME WITHOUT TIME ZONE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    note TEXT,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_routes_tenant_route_code_lower
    ON routes (tenant_id, lower(route_code));

CREATE INDEX IF NOT EXISTS idx_routes_origin_hub_id ON routes(origin_hub_id);
CREATE INDEX IF NOT EXISTS idx_routes_destination_hub_id ON routes(destination_hub_id);
CREATE INDEX IF NOT EXISTS idx_routes_destination_post_office_code ON routes(destination_post_office_code);
CREATE INDEX IF NOT EXISTS idx_routes_vehicle_id ON routes(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_routes_status ON routes(status);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_routes_destination_hub_or_post_office'
    ) THEN
        ALTER TABLE routes
            ADD CONSTRAINT ck_routes_destination_hub_or_post_office
            CHECK (
                (destination_type = 'HUB' AND destination_hub_id IS NOT NULL AND destination_post_office_code IS NULL)
                OR
                (destination_type = 'POST_OFFICE' AND destination_hub_id IS NULL AND destination_post_office_code IS NOT NULL)
            );
    END IF;
END $$;

COMMENT ON TABLE routes IS 'Bảng lưu tuyến vận chuyển cố định giữa Hub-Hub và Hub-Bưu cục';
