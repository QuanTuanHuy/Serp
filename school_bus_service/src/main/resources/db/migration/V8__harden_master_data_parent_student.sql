-- ============================================================
-- V8: Harden Master Data & Parent/Student
-- 1. School coordinate range checks
-- 2. PickupPoint usage_type NOT NULL
-- 3. SchoolPickupPoint window & distance checks
-- 4. Bus home_depot_id index
-- 5. Student: class_name, default_dropoff_point_id
-- 6. SchoolSchedule unique default per school
-- ============================================================

-- ========== 1. School coordinate range ==========

ALTER TABLE school_bus_school
    DROP CONSTRAINT IF EXISTS chk_school_latitude_range;

ALTER TABLE school_bus_school
    ADD CONSTRAINT chk_school_latitude_range
    CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90));

ALTER TABLE school_bus_school
    DROP CONSTRAINT IF EXISTS chk_school_longitude_range;

ALTER TABLE school_bus_school
    ADD CONSTRAINT chk_school_longitude_range
    CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

-- ========== 2. PickupPoint usage_type NOT NULL ==========

-- Backfill before applying NOT NULL
UPDATE school_bus_pickup_point
SET usage_type = 'PICKUP_DROPOFF'
WHERE usage_type IS NULL;

ALTER TABLE school_bus_pickup_point
    ALTER COLUMN usage_type SET NOT NULL;

ALTER TABLE school_bus_pickup_point
    ALTER COLUMN usage_type SET DEFAULT 'PICKUP_DROPOFF';

-- ========== 3. SchoolPickupPoint window & distance checks ==========

ALTER TABLE school_bus_school_pickup_point
    DROP CONSTRAINT IF EXISTS chk_spp_morning_window;

ALTER TABLE school_bus_school_pickup_point
    ADD CONSTRAINT chk_spp_morning_window
    CHECK (
        morning_pickup_window_start IS NULL
        OR morning_pickup_window_end IS NULL
        OR morning_pickup_window_end >= morning_pickup_window_start
    );

ALTER TABLE school_bus_school_pickup_point
    DROP CONSTRAINT IF EXISTS chk_spp_afternoon_window;

ALTER TABLE school_bus_school_pickup_point
    ADD CONSTRAINT chk_spp_afternoon_window
    CHECK (
        afternoon_dropoff_window_start IS NULL
        OR afternoon_dropoff_window_end IS NULL
        OR afternoon_dropoff_window_end >= afternoon_dropoff_window_start
    );

ALTER TABLE school_bus_school_pickup_point
    DROP CONSTRAINT IF EXISTS chk_spp_distance_positive;

ALTER TABLE school_bus_school_pickup_point
    ADD CONSTRAINT chk_spp_distance_positive
    CHECK (estimated_distance_to_school_km IS NULL OR estimated_distance_to_school_km >= 0);

ALTER TABLE school_bus_school_pickup_point
    DROP CONSTRAINT IF EXISTS chk_spp_duration_positive;

ALTER TABLE school_bus_school_pickup_point
    ADD CONSTRAINT chk_spp_duration_positive
    CHECK (estimated_duration_to_school_min IS NULL OR estimated_duration_to_school_min >= 0);

-- ========== 4. Bus home_depot_id index ==========

CREATE INDEX IF NOT EXISTS idx_bus_home_depot
    ON school_bus_bus (home_depot_id)
    WHERE home_depot_id IS NOT NULL;

-- ========== 5. Student: class_name, default_dropoff_point_id ==========

ALTER TABLE school_bus_student
    ADD COLUMN IF NOT EXISTS class_name varchar(50);

ALTER TABLE school_bus_student
    ADD COLUMN IF NOT EXISTS default_dropoff_point_id bigint;

ALTER TABLE school_bus_student
    DROP CONSTRAINT IF EXISTS fk_student_default_dropoff_point;

ALTER TABLE school_bus_student
    ADD CONSTRAINT fk_student_default_dropoff_point
    FOREIGN KEY (default_dropoff_point_id)
    REFERENCES school_bus_pickup_point(id);

-- Comment: pickup_point_id is the legacy "default pickup point"
COMMENT ON COLUMN school_bus_student.pickup_point_id
    IS 'Default pickup point for this student (legacy column name)';

COMMENT ON COLUMN school_bus_student.default_dropoff_point_id
    IS 'Default drop-off point for this student';

COMMENT ON COLUMN school_bus_student.class_name
    IS 'Class name within the grade, e.g. 1A, 2B';

-- ========== 6. SchoolSchedule unique default per school ==========

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_schedule_default_active
    ON school_bus_school_schedule (tenant_id, school_id)
    WHERE is_default = true AND is_active = true AND is_deleted = false;
