ALTER TABLE handover_manifests
    ADD COLUMN IF NOT EXISTS assigned_driver_id BIGINT,
    ADD COLUMN IF NOT EXISTS planned_departure_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS planned_arrival_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS origin_post_office_latitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS origin_post_office_longitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS driver_start_checkin_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS driver_start_latitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS driver_start_longitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS driver_start_distance_m DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS driver_end_checkin_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS driver_end_latitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS driver_end_longitude DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS driver_end_distance_m DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_handover_manifests_driver_id
    ON handover_manifests (assigned_driver_id);

CREATE INDEX IF NOT EXISTS idx_handover_manifests_planned_window
    ON handover_manifests (planned_departure_at, planned_arrival_at);
