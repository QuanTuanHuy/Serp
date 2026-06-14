/*
Author: Nguyen The Anh
Description: Part of Serp Project - Table Bags
*/

CREATE TABLE IF NOT EXISTS bags (
    id BIGSERIAL PRIMARY KEY,
    bag_code VARCHAR(100) NOT NULL,
    origin_hub_id BIGINT NOT NULL,
    destination_type VARCHAR(30) NOT NULL,
    destination_hub_id BIGINT,
    destination_post_office_code VARCHAR(255),
    vehicle_id BIGINT,
    route_id BIGINT,
    max_weight DOUBLE PRECISION NOT NULL DEFAULT 50,
    max_volume DOUBLE PRECISION NOT NULL DEFAULT 0.5,
    max_orders INT NOT NULL DEFAULT 30,
    current_weight DOUBLE PRECISION NOT NULL DEFAULT 0,
    current_volume DOUBLE PRECISION NOT NULL DEFAULT 0,
    current_orders INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    sealed_at TIMESTAMP WITHOUT TIME ZONE,
    note TEXT,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_bags_tenant_bag_code_lower
    ON bags (tenant_id, lower(bag_code));

CREATE INDEX IF NOT EXISTS idx_bags_origin_hub_id ON bags(origin_hub_id);
CREATE INDEX IF NOT EXISTS idx_bags_destination_hub_id ON bags(destination_hub_id);
CREATE INDEX IF NOT EXISTS idx_bags_destination_post_office_code ON bags(destination_post_office_code);
CREATE INDEX IF NOT EXISTS idx_bags_vehicle_id ON bags(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_bags_route_id ON bags(route_id);
CREATE INDEX IF NOT EXISTS idx_bags_status ON bags(status);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_bags_destination_hub_or_post_office'
    ) THEN
        ALTER TABLE bags
            ADD CONSTRAINT ck_bags_destination_hub_or_post_office
            CHECK (
                (destination_type = 'HUB' AND destination_hub_id IS NOT NULL AND destination_post_office_code IS NULL)
                OR
                (destination_type = 'POST_OFFICE' AND destination_hub_id IS NULL AND destination_post_office_code IS NOT NULL)
            );
    END IF;
END $$;

COMMENT ON TABLE bags IS 'Bảng lưu trữ thông tin túi hàng trung chuyển ở chặng second-mile';
