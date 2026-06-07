-- V17: Route stop terminal model
-- Replaces stop_type (PICKUP/DROPOFF) with location_type + stop_purpose.
-- Makes pickup_point_id nullable to allow SCHOOL and DEPOT terminal stops.
-- Data will be truncated by operator before this migration runs.

-- 1. Drop old stop_type constraint and column
ALTER TABLE school_bus_route_stop DROP CONSTRAINT IF EXISTS chk_route_stop_type;
ALTER TABLE school_bus_route_stop DROP COLUMN IF EXISTS stop_type;

-- 2. Make pickup_point_id nullable (terminal stops have no pickup point)
ALTER TABLE school_bus_route_stop ALTER COLUMN pickup_point_id DROP NOT NULL;

-- 3. Add new columns
ALTER TABLE school_bus_route_stop
    ADD COLUMN IF NOT EXISTS location_type VARCHAR(30) NOT NULL DEFAULT 'PICKUP_POINT',
    ADD COLUMN IF NOT EXISTS stop_purpose  VARCHAR(30) NOT NULL DEFAULT 'PICKUP',
    ADD COLUMN IF NOT EXISTS school_id     BIGINT,
    ADD COLUMN IF NOT EXISTS depot_id      BIGINT;

-- 4. Remove defaults after column addition (columns already get values from existing rows)
ALTER TABLE school_bus_route_stop
    ALTER COLUMN location_type DROP DEFAULT,
    ALTER COLUMN stop_purpose  DROP DEFAULT;

-- 5. Foreign key constraints for school_id and depot_id
ALTER TABLE school_bus_route_stop
    DROP CONSTRAINT IF EXISTS fk_route_stop_school,
    DROP CONSTRAINT IF EXISTS fk_route_stop_depot;

ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT fk_route_stop_school
        FOREIGN KEY (school_id) REFERENCES school_bus_school(id);

ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT fk_route_stop_depot
        FOREIGN KEY (depot_id) REFERENCES school_bus_depot(id);

-- 6. Check constraints
ALTER TABLE school_bus_route_stop
    DROP CONSTRAINT IF EXISTS chk_route_stop_location_type,
    DROP CONSTRAINT IF EXISTS chk_route_stop_purpose,
    DROP CONSTRAINT IF EXISTS chk_route_stop_location_ref;

ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT chk_route_stop_location_type
        CHECK (location_type IN ('DEPOT', 'SCHOOL', 'PICKUP_POINT'));

ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT chk_route_stop_purpose
        CHECK (stop_purpose IN ('START_TERMINAL', 'PICKUP', 'DROPOFF', 'END_TERMINAL'));

ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT chk_route_stop_location_ref
        CHECK (
            (location_type = 'PICKUP_POINT' AND pickup_point_id IS NOT NULL)
            OR (location_type = 'SCHOOL'       AND school_id IS NOT NULL)
            OR (location_type = 'DEPOT'        AND depot_id  IS NOT NULL)
        );

-- 7. Indexes
CREATE INDEX IF NOT EXISTS idx_route_stop_school
    ON school_bus_route_stop (school_id) WHERE school_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_route_stop_depot
    ON school_bus_route_stop (depot_id) WHERE depot_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_route_stop_purpose
    ON school_bus_route_stop (route_id, stop_purpose);
