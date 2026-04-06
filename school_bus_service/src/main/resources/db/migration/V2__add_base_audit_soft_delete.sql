DO $$
DECLARE
    v_table_name TEXT;
BEGIN
    FOREACH v_table_name IN ARRAY ARRAY[
        'school_bus_school',
        'school_bus_parent_profile',
        'school_bus_bus',
        'school_bus_driver_profile',
        'school_bus_attendant_profile',
        'school_bus_pickup_point',
        'school_bus_student',
        'school_bus_transport_request',
        'school_bus_request_student',
        'school_bus_route_plan',
        'school_bus_route_stop',
        'school_bus_route_assignment',
        'school_bus_attendance',
        'school_bus_trip_history',
        'school_bus_audit_log'
    ]
    LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = v_table_name
              AND column_name = 'created_stamp'
        ) THEN
            EXECUTE format('ALTER TABLE %I RENAME COLUMN created_stamp TO created_at', v_table_name);
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = v_table_name
              AND column_name = 'last_updated_stamp'
        ) THEN
            EXECUTE format('ALTER TABLE %I RENAME COLUMN last_updated_stamp TO updated_at', v_table_name);
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_name = v_table_name
              AND column_name = 'active'
        ) THEN
            EXECUTE format('ALTER TABLE %I RENAME COLUMN active TO is_active', v_table_name);
        END IF;

        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE', v_table_name);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE', v_table_name);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP', v_table_name);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS created_by VARCHAR(100)', v_table_name);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP', v_table_name);
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100)', v_table_name);
        EXECUTE format('UPDATE %I SET is_deleted = FALSE WHERE is_deleted IS NULL', v_table_name);
        EXECUTE format('UPDATE %I SET is_active = TRUE WHERE is_active IS NULL', v_table_name);
        EXECUTE format('UPDATE %I SET updated_at = created_at WHERE updated_at IS NULL', v_table_name);
    END LOOP;
END $$;

ALTER TABLE school_bus_request_student
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

ALTER TABLE school_bus_route_stop
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

ALTER TABLE school_bus_route_assignment
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

UPDATE school_bus_request_student rs
SET tenant_id = tr.tenant_id
FROM school_bus_transport_request tr
WHERE rs.request_id = tr.id
  AND rs.tenant_id IS NULL;

UPDATE school_bus_route_stop stop
SET tenant_id = rp.tenant_id
FROM school_bus_route_plan rp
WHERE stop.route_id = rp.id
  AND stop.tenant_id IS NULL;

UPDATE school_bus_route_assignment assignment
SET tenant_id = rp.tenant_id
FROM school_bus_route_plan rp
WHERE assignment.route_id = rp.id
  AND assignment.tenant_id IS NULL;

ALTER TABLE school_bus_request_student
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE school_bus_route_stop
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE school_bus_route_assignment
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_school_bus_school_tenant_deleted
    ON school_bus_school (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_school_tenant_active_deleted
    ON school_bus_school (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_parent_profile_tenant_deleted
    ON school_bus_parent_profile (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_parent_profile_tenant_active_deleted
    ON school_bus_parent_profile (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_bus_tenant_deleted
    ON school_bus_bus (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_bus_tenant_active_deleted
    ON school_bus_bus (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_driver_profile_tenant_deleted
    ON school_bus_driver_profile (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_driver_profile_tenant_active_deleted
    ON school_bus_driver_profile (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_attendant_profile_tenant_deleted
    ON school_bus_attendant_profile (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_attendant_profile_tenant_active_deleted
    ON school_bus_attendant_profile (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_pickup_point_tenant_deleted
    ON school_bus_pickup_point (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_pickup_point_tenant_active_deleted
    ON school_bus_pickup_point (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_student_tenant_deleted
    ON school_bus_student (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_student_tenant_active_deleted
    ON school_bus_student (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_transport_request_tenant_deleted
    ON school_bus_transport_request (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_transport_request_tenant_active_deleted
    ON school_bus_transport_request (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_request_student_tenant_deleted
    ON school_bus_request_student (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_request_student_tenant_active_deleted
    ON school_bus_request_student (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_route_plan_tenant_deleted
    ON school_bus_route_plan (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_route_plan_tenant_active_deleted
    ON school_bus_route_plan (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_route_stop_tenant_deleted
    ON school_bus_route_stop (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_route_stop_tenant_active_deleted
    ON school_bus_route_stop (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_route_assignment_tenant_deleted
    ON school_bus_route_assignment (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_route_assignment_tenant_active_deleted
    ON school_bus_route_assignment (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_attendance_tenant_deleted
    ON school_bus_attendance (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_attendance_tenant_active_deleted
    ON school_bus_attendance (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_trip_history_tenant_deleted
    ON school_bus_trip_history (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_trip_history_tenant_active_deleted
    ON school_bus_trip_history (tenant_id, is_active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_school_bus_audit_log_tenant_deleted
    ON school_bus_audit_log (tenant_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_school_bus_audit_log_tenant_active_deleted
    ON school_bus_audit_log (tenant_id, is_active, is_deleted);
