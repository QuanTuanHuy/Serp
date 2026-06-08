/*
Author: Nguyen The Anh
Description: Part of Serp Project - Bag distribution manifests
*/

CREATE TABLE IF NOT EXISTS bag_distribution_manifests (
    id BIGSERIAL PRIMARY KEY,
    manifest_code VARCHAR(100) NOT NULL,
    origin_hub_id BIGINT NOT NULL,
    destination_type VARCHAR(30) NOT NULL,
    destination_hub_id BIGINT,
    destination_post_office_code VARCHAR(255),
    route_id BIGINT,
    vehicle_id BIGINT,
    assigned_driver_id BIGINT,
    planned_departure_at TIMESTAMP WITHOUT TIME ZONE,
    planned_arrival_at TIMESTAMP WITHOUT TIME ZONE,
    actual_departure_at TIMESTAMP WITHOUT TIME ZONE,
    actual_arrival_at TIMESTAMP WITHOUT TIME ZONE,
    driver_start_latitude DOUBLE PRECISION,
    driver_start_longitude DOUBLE PRECISION,
    driver_start_distance_m DOUBLE PRECISION,
    driver_start_photo_url TEXT,
    driver_end_latitude DOUBLE PRECISION,
    driver_end_longitude DOUBLE PRECISION,
    driver_end_distance_m DOUBLE PRECISION,
    driver_end_photo_url TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'CREATED',
    note TEXT,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_bag_distribution_manifests_tenant_code
    ON bag_distribution_manifests (tenant_id, lower(manifest_code));

CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifests_tenant_status_origin
    ON bag_distribution_manifests (tenant_id, status, origin_hub_id);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifests_tenant_route
    ON bag_distribution_manifests (tenant_id, route_id);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifests_tenant_vehicle
    ON bag_distribution_manifests (tenant_id, vehicle_id);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifests_tenant_driver
    ON bag_distribution_manifests (tenant_id, assigned_driver_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_bag_distribution_destination'
    ) THEN
        ALTER TABLE bag_distribution_manifests
            ADD CONSTRAINT ck_bag_distribution_destination
            CHECK (
                (destination_type = 'HUB' AND destination_hub_id IS NOT NULL AND destination_post_office_code IS NULL)
                OR
                (destination_type = 'POST_OFFICE' AND destination_hub_id IS NULL AND destination_post_office_code IS NOT NULL)
            );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS bag_distribution_manifest_bags (
    id BIGSERIAL PRIMARY KEY,
    manifest_id BIGINT NOT NULL,
    bag_id BIGINT NOT NULL,
    bag_code VARCHAR(100) NOT NULL,
    origin_hub_id BIGINT NOT NULL,
    destination_type VARCHAR(30) NOT NULL,
    destination_hub_id BIGINT,
    destination_post_office_code VARCHAR(255),
    total_weight_snapshot DOUBLE PRECISION,
    total_volume_snapshot DOUBLE PRECISION,
    total_orders_snapshot INTEGER,
    scan_out_time TIMESTAMP WITHOUT TIME ZONE,
    scan_in_time TIMESTAMP WITHOUT TIME ZONE,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_bag_distribution_manifest_bag UNIQUE (manifest_id, bag_id),
    CONSTRAINT fk_bag_distribution_manifest_bags_manifest
        FOREIGN KEY (manifest_id) REFERENCES bag_distribution_manifests(id) ON DELETE CASCADE,
    CONSTRAINT fk_bag_distribution_manifest_bags_bag
        FOREIGN KEY (bag_id) REFERENCES bags(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifest_bags_manifest
    ON bag_distribution_manifest_bags (manifest_id);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifest_bags_bag
    ON bag_distribution_manifest_bags (bag_id);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifest_bags_tenant_bag
    ON bag_distribution_manifest_bags (tenant_id, bag_id);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifest_bags_scan_out
    ON bag_distribution_manifest_bags (scan_out_time);
CREATE INDEX IF NOT EXISTS idx_bag_distribution_manifest_bags_scan_in
    ON bag_distribution_manifest_bags (scan_in_time);

COMMENT ON TABLE bag_distribution_manifests IS 'Bag-level route run manifests from hub to hub or hub to post office';
COMMENT ON TABLE bag_distribution_manifest_bags IS 'Bag snapshots assigned to bag distribution manifests';
