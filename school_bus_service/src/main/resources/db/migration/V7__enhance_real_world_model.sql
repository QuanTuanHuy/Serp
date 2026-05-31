-- ============================================================
-- V7: Enhance School Bus with real-world model
-- Phase 1: Depot code + Bus-Depot relationship
-- Phase 2: SchoolSchedule
-- Phase 3: PickupPoint N-N School
-- Phase 4: TripExecution start/end snapshot
-- ============================================================

-- ===================== PHASE 1: DEPOT =====================

-- 1a. Depot code
ALTER TABLE school_bus_depot
    ADD COLUMN IF NOT EXISTS code varchar(100);

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_depot_tenant_code
    ON school_bus_depot (tenant_id, code)
    WHERE code IS NOT NULL AND is_deleted = false;

-- 1b. Bus home depot
ALTER TABLE school_bus_bus
    ADD COLUMN IF NOT EXISTS home_depot_id bigint;

ALTER TABLE school_bus_bus
    DROP CONSTRAINT IF EXISTS fk_school_bus_bus_home_depot;

ALTER TABLE school_bus_bus
    ADD CONSTRAINT fk_school_bus_bus_home_depot
    FOREIGN KEY (home_depot_id)
    REFERENCES school_bus_depot(id);

-- ================== PHASE 2: SCHOOL SCHEDULE ==================

CREATE TABLE IF NOT EXISTS school_bus_school_schedule
(
    id                bigserial PRIMARY KEY,
    tenant_id         bigint       NOT NULL,

    school_id         bigint       NOT NULL
        REFERENCES school_bus_school(id),

    schedule_code     varchar(100),
    schedule_name     varchar(255) NOT NULL,

    education_level   varchar(50),
    grade             varchar(50),
    shift_type        varchar(50)  NOT NULL,

    day_of_week       varchar(20),

    arrival_deadline  time,
    departure_time    time,

    effective_from    date         NOT NULL,
    effective_to      date,

    is_default        boolean   DEFAULT false             NOT NULL,
    is_active         boolean   DEFAULT true              NOT NULL,
    is_deleted        boolean   DEFAULT false             NOT NULL,

    created_at        timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by        varchar(100),
    updated_at        timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by        varchar(100),

    CONSTRAINT chk_school_schedule_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_schedule_code
    ON school_bus_school_schedule (tenant_id, schedule_code)
    WHERE schedule_code IS NOT NULL AND is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_school_schedule_tenant_deleted
    ON school_bus_school_schedule (tenant_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_schedule_school
    ON school_bus_school_schedule (school_id, is_deleted);

-- Subscription links to schedule
ALTER TABLE school_bus_student_subscription
    ADD COLUMN IF NOT EXISTS school_schedule_id bigint;

ALTER TABLE school_bus_student_subscription
    DROP CONSTRAINT IF EXISTS fk_subscription_school_schedule;

ALTER TABLE school_bus_student_subscription
    ADD CONSTRAINT fk_subscription_school_schedule
    FOREIGN KEY (school_schedule_id)
    REFERENCES school_bus_school_schedule(id);

-- ============= PHASE 3: PICKUP POINT N-N SCHOOL =============

-- 3a. Enrich pickup point
ALTER TABLE school_bus_pickup_point
    ADD COLUMN IF NOT EXISTS code               varchar(100),
    ADD COLUMN IF NOT EXISTS zone_code           varchar(100),
    ADD COLUMN IF NOT EXISTS usage_type          varchar(30) DEFAULT 'PICKUP_DROPOFF',
    ADD COLUMN IF NOT EXISTS pickup_instruction  text;

-- Make school_id nullable for shared pickup points
ALTER TABLE school_bus_pickup_point
    ALTER COLUMN school_id DROP NOT NULL;

ALTER TABLE school_bus_pickup_point
    DROP CONSTRAINT IF EXISTS chk_pickup_point_usage_type;

ALTER TABLE school_bus_pickup_point
    ADD CONSTRAINT chk_pickup_point_usage_type
    CHECK (usage_type IN ('PICKUP_ONLY', 'DROPOFF_ONLY', 'PICKUP_DROPOFF'));

ALTER TABLE school_bus_pickup_point
    DROP CONSTRAINT IF EXISTS chk_pickup_point_latitude_range;

ALTER TABLE school_bus_pickup_point
    ADD CONSTRAINT chk_pickup_point_latitude_range
    CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90));

ALTER TABLE school_bus_pickup_point
    DROP CONSTRAINT IF EXISTS chk_pickup_point_longitude_range;

ALTER TABLE school_bus_pickup_point
    ADD CONSTRAINT chk_pickup_point_longitude_range
    CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

CREATE UNIQUE INDEX IF NOT EXISTS uk_pickup_point_tenant_code
    ON school_bus_pickup_point (tenant_id, code)
    WHERE code IS NOT NULL AND is_deleted = false;

-- 3b. School ↔ PickupPoint junction table
CREATE TABLE IF NOT EXISTS school_bus_school_pickup_point
(
    id                                  bigserial PRIMARY KEY,
    tenant_id                           bigint   NOT NULL,

    school_id                           bigint   NOT NULL
        REFERENCES school_bus_school(id),

    pickup_point_id                     bigint   NOT NULL
        REFERENCES school_bus_pickup_point(id),

    morning_pickup_window_start         time,
    morning_pickup_window_end           time,
    afternoon_dropoff_window_start      time,
    afternoon_dropoff_window_end        time,

    estimated_distance_to_school_km     double precision,
    estimated_duration_to_school_min    integer,

    is_default                          boolean   DEFAULT false             NOT NULL,
    is_active                           boolean   DEFAULT true              NOT NULL,
    is_deleted                          boolean   DEFAULT false             NOT NULL,

    created_at                          timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by                          varchar(100),
    updated_at                          timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by                          varchar(100)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_pickup_point_active
    ON school_bus_school_pickup_point (tenant_id, school_id, pickup_point_id)
    WHERE is_deleted = false;

CREATE INDEX IF NOT EXISTS idx_school_pickup_point_tenant_deleted
    ON school_bus_school_pickup_point (tenant_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_pickup_point_school
    ON school_bus_school_pickup_point (school_id, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_pickup_point_pickup
    ON school_bus_school_pickup_point (pickup_point_id, is_deleted);

-- 3c. Backfill existing school-pickup relations
INSERT INTO school_bus_school_pickup_point
(
    tenant_id,
    school_id,
    pickup_point_id,
    morning_pickup_window_start,
    morning_pickup_window_end,
    is_default,
    is_active,
    is_deleted,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    pp.tenant_id,
    pp.school_id,
    pp.id,
    pp.pickup_window_start,
    pp.pickup_window_end,
    true,
    pp.is_active,
    pp.is_deleted,
    pp.created_at,
    pp.created_by,
    pp.updated_at,
    pp.updated_by
FROM school_bus_pickup_point pp
WHERE pp.school_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM school_bus_school_pickup_point spp
    WHERE spp.tenant_id = pp.tenant_id
      AND spp.school_id = pp.school_id
      AND spp.pickup_point_id = pp.id
  );

-- ========= PHASE 4: TRIP EXECUTION SNAPSHOT =========

ALTER TABLE school_bus_trip_execution
    ADD COLUMN IF NOT EXISTS start_location_type varchar(30),
    ADD COLUMN IF NOT EXISTS start_school_id     bigint,
    ADD COLUMN IF NOT EXISTS start_depot_id      bigint,
    ADD COLUMN IF NOT EXISTS end_location_type   varchar(30),
    ADD COLUMN IF NOT EXISTS end_school_id       bigint,
    ADD COLUMN IF NOT EXISTS end_depot_id        bigint;

ALTER TABLE school_bus_trip_execution
    DROP CONSTRAINT IF EXISTS fk_trip_start_school;

ALTER TABLE school_bus_trip_execution
    ADD CONSTRAINT fk_trip_start_school
    FOREIGN KEY (start_school_id)
    REFERENCES school_bus_school(id);

ALTER TABLE school_bus_trip_execution
    DROP CONSTRAINT IF EXISTS fk_trip_start_depot;

ALTER TABLE school_bus_trip_execution
    ADD CONSTRAINT fk_trip_start_depot
    FOREIGN KEY (start_depot_id)
    REFERENCES school_bus_depot(id);

ALTER TABLE school_bus_trip_execution
    DROP CONSTRAINT IF EXISTS fk_trip_end_school;

ALTER TABLE school_bus_trip_execution
    ADD CONSTRAINT fk_trip_end_school
    FOREIGN KEY (end_school_id)
    REFERENCES school_bus_school(id);

ALTER TABLE school_bus_trip_execution
    DROP CONSTRAINT IF EXISTS fk_trip_end_depot;

ALTER TABLE school_bus_trip_execution
    ADD CONSTRAINT fk_trip_end_depot
    FOREIGN KEY (end_depot_id)
    REFERENCES school_bus_depot(id);
