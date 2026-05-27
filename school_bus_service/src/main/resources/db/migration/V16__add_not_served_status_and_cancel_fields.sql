-- V10: Add NOT_SERVED to trip_student status; add cancellation fields to trip_execution

-- 1. Update trip_student status check constraint to include NOT_SERVED.
--    NOT_SERVED = stop or trip was skipped/cancelled before the student could board.
--    Distinct from NO_SHOW (bus visited, student was not present).
ALTER TABLE school_bus_trip_student DROP CONSTRAINT IF EXISTS chk_trip_student_status;
ALTER TABLE school_bus_trip_student
    ADD CONSTRAINT chk_trip_student_status
        CHECK (status IN ('PLANNED', 'BOARDED', 'ABSENT', 'DROPPED_OFF', 'NO_SHOW', 'NOT_SERVED'));

-- 2. Add cancellation fields to trip_execution.
--    These replace the earlier workaround of storing cancellation reason in completion_note.
ALTER TABLE school_bus_trip_execution
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

-- BIGINT to match the convention of published_by, assigned_by, approved_by (all BIGINT actor IDs).
ALTER TABLE school_bus_trip_execution
    ADD COLUMN IF NOT EXISTS cancelled_by BIGINT;

ALTER TABLE school_bus_trip_execution
    ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;
