-- Author: Nguyen The Anh
-- Description: Centralize second-mile driver check-in records

CREATE TABLE IF NOT EXISTS checkin (
    id BIGSERIAL PRIMARY KEY,
    checkin_type VARCHAR(40) NOT NULL,
    bag_distribution_manifest_id BIGINT,
    driver_staff_id BIGINT NOT NULL,
    checkin_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    checkin_location geography(Point, 4326) NOT NULL,
    distance_m DOUBLE PRECISION,
    allowed_radius_m DOUBLE PRECISION,
    location_label TEXT,
    photo_url TEXT NOT NULL,
    tenant_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_checkin_bag_distribution_manifest_type
    ON checkin (tenant_id, bag_distribution_manifest_id, checkin_type)
    WHERE bag_distribution_manifest_id IS NOT NULL
      AND checkin_type IN ('BAG_DISTRIBUTION_START', 'BAG_DISTRIBUTION_END');

CREATE INDEX IF NOT EXISTS idx_checkin_tenant_type
    ON checkin (tenant_id, checkin_type);

CREATE INDEX IF NOT EXISTS idx_checkin_tenant_bag_distribution_manifest
    ON checkin (tenant_id, bag_distribution_manifest_id);

CREATE INDEX IF NOT EXISTS idx_checkin_location
    ON checkin USING GIST (checkin_location);

INSERT INTO checkin (
    checkin_type,
    bag_distribution_manifest_id,
    driver_staff_id,
    checkin_time,
    checkin_location,
    distance_m,
    allowed_radius_m,
    photo_url,
    tenant_id,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    'BAG_DISTRIBUTION_START',
    manifest.id,
    manifest.assigned_driver_id,
    COALESCE(manifest.actual_departure_at, manifest.updated_at, CURRENT_TIMESTAMP),
    ST_SetSRID(ST_MakePoint(manifest.driver_start_longitude, manifest.driver_start_latitude), 4326)::geography,
    manifest.driver_start_distance_m,
    100.0,
    manifest.driver_start_photo_url,
    manifest.tenant_id,
    COALESCE(manifest.created_at, CURRENT_TIMESTAMP),
    COALESCE(manifest.updated_at, CURRENT_TIMESTAMP),
    manifest.created_by,
    manifest.updated_by
FROM bag_distribution_manifests manifest
WHERE manifest.driver_start_photo_url IS NOT NULL
  AND manifest.driver_start_latitude IS NOT NULL
  AND manifest.driver_start_longitude IS NOT NULL
  AND manifest.assigned_driver_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM checkin existing
      WHERE existing.tenant_id IS NOT DISTINCT FROM manifest.tenant_id
        AND existing.bag_distribution_manifest_id = manifest.id
        AND existing.checkin_type = 'BAG_DISTRIBUTION_START'
  );

INSERT INTO checkin (
    checkin_type,
    bag_distribution_manifest_id,
    driver_staff_id,
    checkin_time,
    checkin_location,
    distance_m,
    allowed_radius_m,
    photo_url,
    tenant_id,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT
    'BAG_DISTRIBUTION_END',
    manifest.id,
    manifest.assigned_driver_id,
    COALESCE(manifest.actual_arrival_at, manifest.updated_at, CURRENT_TIMESTAMP),
    ST_SetSRID(ST_MakePoint(manifest.driver_end_longitude, manifest.driver_end_latitude), 4326)::geography,
    manifest.driver_end_distance_m,
    CASE WHEN manifest.destination_type = 'HUB' THEN 100.0 ELSE NULL END,
    manifest.driver_end_photo_url,
    manifest.tenant_id,
    COALESCE(manifest.created_at, CURRENT_TIMESTAMP),
    COALESCE(manifest.updated_at, CURRENT_TIMESTAMP),
    manifest.created_by,
    manifest.updated_by
FROM bag_distribution_manifests manifest
WHERE manifest.driver_end_photo_url IS NOT NULL
  AND manifest.driver_end_latitude IS NOT NULL
  AND manifest.driver_end_longitude IS NOT NULL
  AND manifest.assigned_driver_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM checkin existing
      WHERE existing.tenant_id IS NOT DISTINCT FROM manifest.tenant_id
        AND existing.bag_distribution_manifest_id = manifest.id
        AND existing.checkin_type = 'BAG_DISTRIBUTION_END'
  );

COMMENT ON TABLE checkin IS 'Centralized check-in records for second-mile operational flows';
