-- V9: Cleanup pickup point legacy fields + schedule multi-day support
-- =====================================================================

-- =====================================================================
-- 1. DROP LEGACY COLUMNS FROM school_bus_pickup_point
-- =====================================================================
-- school_id is wrong: pickup point is a physical location shared across schools.
-- pickup_window_start/end belong in school_bus_school_pickup_point.

-- Drop FK constraint on school_id if exists
ALTER TABLE school_bus_pickup_point DROP CONSTRAINT IF EXISTS fk_pickup_point_school;
ALTER TABLE school_bus_pickup_point DROP COLUMN IF EXISTS school_id;
ALTER TABLE school_bus_pickup_point DROP COLUMN IF EXISTS pickup_window_start;
ALTER TABLE school_bus_pickup_point DROP COLUMN IF EXISTS pickup_window_end;

-- =====================================================================
-- 2. CREATE school_bus_school_schedule_day TABLE
-- =====================================================================
CREATE TABLE IF NOT EXISTS school_bus_school_schedule_day
(
    id                  bigserial    PRIMARY KEY,
    tenant_id           bigint       NOT NULL,
    school_schedule_id  bigint       NOT NULL
        REFERENCES school_bus_school_schedule (id),
    day_of_week         varchar(20)  NOT NULL,
    is_active           boolean      DEFAULT true  NOT NULL,
    is_deleted          boolean      DEFAULT false NOT NULL,
    created_at          timestamp    DEFAULT current_timestamp NOT NULL,
    created_by          varchar(100),
    updated_at          timestamp    DEFAULT current_timestamp NOT NULL,
    updated_by          varchar(100),

    CONSTRAINT chk_school_schedule_day_of_week
        CHECK (day_of_week IN (
            'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY',
            'SATURDAY', 'SUNDAY'
        ))
);

-- Soft unique: (tenant, schedule, day) when not deleted
CREATE UNIQUE INDEX IF NOT EXISTS uk_school_schedule_day_active
    ON school_bus_school_schedule_day (tenant_id, school_schedule_id, day_of_week)
    WHERE is_deleted = false;

-- =====================================================================
-- 3. BACKFILL from school_bus_school_schedule.day_of_week
-- =====================================================================
-- If day_of_week has a value -> insert that single day.
INSERT INTO school_bus_school_schedule_day (tenant_id, school_schedule_id, day_of_week, created_by, updated_by)
SELECT s.tenant_id, s.id, s.day_of_week, 'V9_MIGRATION', 'V9_MIGRATION'
FROM school_bus_school_schedule s
WHERE s.day_of_week IS NOT NULL
  AND s.is_deleted = false
ON CONFLICT DO NOTHING;

-- If day_of_week is null -> default to MON-FRI.
INSERT INTO school_bus_school_schedule_day (tenant_id, school_schedule_id, day_of_week, created_by, updated_by)
SELECT s.tenant_id, s.id, d.day_of_week, 'V9_MIGRATION', 'V9_MIGRATION'
FROM school_bus_school_schedule s
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY')) AS d(day_of_week)
WHERE s.day_of_week IS NULL
  AND s.is_deleted = false
ON CONFLICT DO NOTHING;

-- =====================================================================
-- 4. DROP legacy day_of_week from school_bus_school_schedule
-- =====================================================================
ALTER TABLE school_bus_school_schedule DROP COLUMN IF EXISTS day_of_week;
