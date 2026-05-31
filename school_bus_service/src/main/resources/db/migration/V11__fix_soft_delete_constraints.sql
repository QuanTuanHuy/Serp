-- V11: Fix unique constraints and FK cascades for soft-delete model
-- =====================================================================
-- All unique constraints must be partial indexes with WHERE is_deleted = false
-- to allow soft-deleted records to not block new inserts.
-- ON DELETE CASCADE must be removed since we use soft-delete everywhere.
-- =====================================================================

-- 1. school_bus_trip_history.route_id - inline UNIQUE from V1 (hard constraint, no is_deleted filter)
ALTER TABLE school_bus_trip_history
    DROP CONSTRAINT IF EXISTS school_bus_trip_history_route_id_key;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_trip_history_route
    ON school_bus_trip_history (route_id)
    WHERE is_deleted = false;

-- 2. Remove ON DELETE CASCADE from V1 FKs (soft-delete model should never cascade hard-delete)

-- 2a. school_bus_request_student.request_id
ALTER TABLE school_bus_request_student
    DROP CONSTRAINT IF EXISTS school_bus_request_student_request_id_fkey;
ALTER TABLE school_bus_request_student
    ADD CONSTRAINT school_bus_request_student_request_id_fkey
    FOREIGN KEY (request_id) REFERENCES school_bus_transport_request(id);

-- 2b. school_bus_route_stop.route_id
ALTER TABLE school_bus_route_stop
    DROP CONSTRAINT IF EXISTS school_bus_route_stop_route_id_fkey;
ALTER TABLE school_bus_route_stop
    ADD CONSTRAINT school_bus_route_stop_route_id_fkey
    FOREIGN KEY (route_id) REFERENCES school_bus_route_plan(id);

-- 2c. school_bus_route_assignment.route_id
ALTER TABLE school_bus_route_assignment
    DROP CONSTRAINT IF EXISTS school_bus_route_assignment_route_id_fkey;
ALTER TABLE school_bus_route_assignment
    ADD CONSTRAINT school_bus_route_assignment_route_id_fkey
    FOREIGN KEY (route_id) REFERENCES school_bus_route_plan(id);

-- 3. school_bus_code_sequence (tenant_id, sequence_key) - hard CONSTRAINT from V4
-- This table is internal sequence tracking and doesn't soft-delete, so we keep it as is.
