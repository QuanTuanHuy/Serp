-- School Bus explicit route start/end schema.
-- Run this manually after truncating school_bus data if you keep Flyway history.

CREATE TABLE IF NOT EXISTS school_bus_depot (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    contact_phone VARCHAR(50),
    description TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_school_bus_depot_latitude
        CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_school_bus_depot_longitude
        CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_school_bus_depot_lat_lng_pair
        CHECK ((latitude IS NULL AND longitude IS NULL) OR (latitude IS NOT NULL AND longitude IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS idx_school_bus_depot_tenant_deleted
    ON school_bus_depot (tenant_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_depot_tenant_active_deleted
    ON school_bus_depot (tenant_id, is_active, is_deleted);

ALTER TABLE school_bus_route_plan
    ADD COLUMN IF NOT EXISTS route_direction VARCHAR(30) NOT NULL DEFAULT 'OUTBOUND',
    ADD COLUMN IF NOT EXISTS start_location_type VARCHAR(30) NOT NULL DEFAULT 'SCHOOL',
    ADD COLUMN IF NOT EXISTS start_school_id BIGINT,
    ADD COLUMN IF NOT EXISTS start_depot_id BIGINT,
    ADD COLUMN IF NOT EXISTS end_location_type VARCHAR(30) NOT NULL DEFAULT 'SCHOOL',
    ADD COLUMN IF NOT EXISTS end_school_id BIGINT,
    ADD COLUMN IF NOT EXISTS end_depot_id BIGINT;

ALTER TABLE school_bus_route_stop
    ADD COLUMN IF NOT EXISTS stop_type VARCHAR(30) NOT NULL DEFAULT 'PICKUP';

ALTER TABLE school_bus_route_plan
    DROP CONSTRAINT IF EXISTS fk_route_plan_start_school,
    DROP CONSTRAINT IF EXISTS fk_route_plan_end_school,
    DROP CONSTRAINT IF EXISTS fk_route_plan_start_depot,
    DROP CONSTRAINT IF EXISTS fk_route_plan_end_depot,
    DROP CONSTRAINT IF EXISTS chk_route_direction,
    DROP CONSTRAINT IF EXISTS chk_route_start_location_type,
    DROP CONSTRAINT IF EXISTS chk_route_end_location_type,
    DROP CONSTRAINT IF EXISTS chk_route_start_location_ref,
    DROP CONSTRAINT IF EXISTS chk_route_end_location_ref,
    DROP CONSTRAINT IF EXISTS chk_route_outbound_end_school,
    DROP CONSTRAINT IF EXISTS chk_route_return_start_school;

ALTER TABLE school_bus_route_stop
    DROP CONSTRAINT IF EXISTS chk_route_stop_type;

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT fk_route_plan_start_school
        FOREIGN KEY (start_school_id) REFERENCES school_bus_school(id);

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT fk_route_plan_end_school
        FOREIGN KEY (end_school_id) REFERENCES school_bus_school(id);

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT fk_route_plan_start_depot
        FOREIGN KEY (start_depot_id) REFERENCES school_bus_depot(id);

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT fk_route_plan_end_depot
        FOREIGN KEY (end_depot_id) REFERENCES school_bus_depot(id);

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_direction
        CHECK (route_direction IN ('OUTBOUND', 'RETURN'));

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_start_location_type
        CHECK (start_location_type IN ('SCHOOL', 'DEPOT'));

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_end_location_type
        CHECK (end_location_type IN ('SCHOOL', 'DEPOT'));

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_start_location_ref
        CHECK (
            (start_location_type = 'SCHOOL' AND start_school_id IS NOT NULL AND start_depot_id IS NULL)
            OR
            (start_location_type = 'DEPOT' AND start_depot_id IS NOT NULL AND start_school_id IS NULL)
        );

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_end_location_ref
        CHECK (
            (end_location_type = 'SCHOOL' AND end_school_id IS NOT NULL AND end_depot_id IS NULL)
            OR
            (end_location_type = 'DEPOT' AND end_depot_id IS NOT NULL AND end_school_id IS NULL)
        );

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_outbound_end_school
        CHECK (route_direction <> 'OUTBOUND' OR end_location_type = 'SCHOOL');

ALTER TABLE school_bus_route_plan
    ADD CONSTRAINT chk_route_return_start_school
        CHECK (route_direction <> 'RETURN' OR start_location_type = 'SCHOOL');

ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT chk_route_stop_type
        CHECK (stop_type IN ('PICKUP', 'DROPOFF'));

CREATE INDEX IF NOT EXISTS idx_route_plan_start_school
    ON school_bus_route_plan (start_school_id);

CREATE INDEX IF NOT EXISTS idx_route_plan_end_school
    ON school_bus_route_plan (end_school_id);

CREATE INDEX IF NOT EXISTS idx_route_plan_start_depot
    ON school_bus_route_plan (start_depot_id);

CREATE INDEX IF NOT EXISTS idx_route_plan_end_depot
    ON school_bus_route_plan (end_depot_id);


-- Add geometry_path
ALTER TABLE school_bus_route_plan ADD COLUMN IF NOT EXISTS geometry_path TEXT;
