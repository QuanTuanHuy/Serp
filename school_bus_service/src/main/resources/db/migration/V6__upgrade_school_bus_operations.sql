-- Upgrade School Bus into an operational dispatch system.
-- This migration is additive/backward-compatible so existing V1/V2 screens can keep running.

ALTER TABLE school_bus_student
    ADD COLUMN IF NOT EXISTS date_of_birth DATE,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(30),
    ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS special_note TEXT;

ALTER TABLE school_bus_transport_request
    ADD COLUMN IF NOT EXISTS request_code VARCHAR(100),
    ADD COLUMN IF NOT EXISTS requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS request_source VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
    ADD COLUMN IF NOT EXISTS change_reason TEXT;

ALTER TABLE school_bus_route_plan
    ADD COLUMN IF NOT EXISTS planned_student_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS assigned_bus_capacity INTEGER,
    ADD COLUMN IF NOT EXISTS route_generation_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN IF NOT EXISTS estimated_cost NUMERIC(14, 2),
    ADD COLUMN IF NOT EXISTS version_no INTEGER NOT NULL DEFAULT 1;

ALTER TABLE school_bus_route_stop
    ADD COLUMN IF NOT EXISTS planned_boarding_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS planned_dropoff_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS estimated_travel_time_from_previous INTEGER,
    ADD COLUMN IF NOT EXISTS distance_from_previous_km DOUBLE PRECISION;

ALTER TABLE school_bus_attendance
    ADD COLUMN IF NOT EXISTS trip_id BIGINT,
    ADD COLUMN IF NOT EXISTS route_stop_id BIGINT,
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS event_source VARCHAR(50) NOT NULL DEFAULT 'MANUAL';

CREATE TABLE IF NOT EXISTS school_bus_student_subscription (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL REFERENCES school_bus_student(id),
    school_id BIGINT NOT NULL REFERENCES school_bus_school(id),
    pickup_point_id BIGINT REFERENCES school_bus_pickup_point(id),
    dropoff_point_id BIGINT REFERENCES school_bus_pickup_point(id),
    subscription_code VARCHAR(100) NOT NULL,
    trip_option VARCHAR(30) NOT NULL,
    is_monday BOOLEAN NOT NULL DEFAULT TRUE,
    is_tuesday BOOLEAN NOT NULL DEFAULT TRUE,
    is_wednesday BOOLEAN NOT NULL DEFAULT TRUE,
    is_thursday BOOLEAN NOT NULL DEFAULT TRUE,
    is_friday BOOLEAN NOT NULL DEFAULT TRUE,
    is_saturday BOOLEAN NOT NULL DEFAULT FALSE,
    is_sunday BOOLEAN NOT NULL DEFAULT FALSE,
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(30) NOT NULL,
    source_request_id BIGINT REFERENCES school_bus_transport_request(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_subscription_effective_range
        CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT chk_subscription_trip_option
        CHECK (trip_option IN ('MORNING', 'AFTERNOON', 'ROUND_TRIP')),
    CONSTRAINT chk_subscription_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'PAUSED', 'STOPPED', 'EXPIRED'))
);

CREATE TABLE IF NOT EXISTS school_bus_transport_request_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL REFERENCES school_bus_transport_request(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    notes TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS school_bus_trip_execution (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    trip_code VARCHAR(100) NOT NULL,
    route_id BIGINT NOT NULL REFERENCES school_bus_route_plan(id),
    service_date DATE NOT NULL,
    route_direction VARCHAR(30) NOT NULL,
    shift_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    planned_start_at TIMESTAMP,
    planned_end_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    planned_distance_km DOUBLE PRECISION,
    planned_duration_min INTEGER,
    actual_distance_km DOUBLE PRECISION,
    actual_duration_min INTEGER,
    completion_note TEXT,
    simulation_mode BOOLEAN NOT NULL DEFAULT FALSE,
    bus_id BIGINT REFERENCES school_bus_bus(id),
    driver_id BIGINT REFERENCES school_bus_driver_profile(id),
    attendant_id BIGINT REFERENCES school_bus_attendant_profile(id),
    route_geometry_path TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_trip_execution_status
        CHECK (status IN ('PLANNED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS school_bus_trip_stop_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL REFERENCES school_bus_trip_execution(id),
    route_stop_id BIGINT NOT NULL REFERENCES school_bus_route_stop(id),
    stop_order INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    actual_arrival_time TIMESTAMP,
    actual_departure_time TIMESTAMP,
    delay_minutes INTEGER,
    actual_boarded_count INTEGER NOT NULL DEFAULT 0,
    actual_dropped_count INTEGER NOT NULL DEFAULT 0,
    note TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_trip_stop_log_status
        CHECK (status IN ('PENDING', 'ARRIVED', 'BOARDING', 'DEPARTED', 'SKIPPED'))
);

CREATE TABLE IF NOT EXISTS school_bus_trip_student (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL REFERENCES school_bus_trip_execution(id),
    student_id BIGINT NOT NULL REFERENCES school_bus_student(id),
    pickup_stop_id BIGINT REFERENCES school_bus_route_stop(id),
    dropoff_stop_id BIGINT REFERENCES school_bus_route_stop(id),
    subscription_id BIGINT REFERENCES school_bus_student_subscription(id),
    status VARCHAR(30) NOT NULL,
    note TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_trip_student_status
        CHECK (status IN ('PLANNED', 'BOARDED', 'ABSENT', 'DROPPED_OFF', 'NO_SHOW'))
);

CREATE TABLE IF NOT EXISTS school_bus_route_assignment_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL REFERENCES school_bus_route_plan(id),
    old_bus_id BIGINT,
    new_bus_id BIGINT,
    old_driver_id BIGINT,
    new_driver_id BIGINT,
    old_attendant_id BIGINT,
    new_attendant_id BIGINT,
    changed_by BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS school_bus_demo_session (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    demo_code VARCHAR(100),
    trip_id BIGINT NOT NULL REFERENCES school_bus_trip_execution(id),
    status VARCHAR(30) NOT NULL,
    speed_multiplier INTEGER NOT NULL DEFAULT 1,
    current_stop_order INTEGER,
    current_latitude DOUBLE PRECISION,
    current_longitude DOUBLE PRECISION,
    progress_percent DOUBLE PRECISION NOT NULL DEFAULT 0,
    started_at TIMESTAMP,
    paused_at TIMESTAMP,
    completed_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    CONSTRAINT chk_demo_session_status
        CHECK (status IN ('READY', 'RUNNING', 'PAUSED', 'COMPLETED', 'STOPPED'))
);

CREATE TABLE IF NOT EXISTS school_bus_demo_event_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    demo_session_id BIGINT NOT NULL REFERENCES school_bus_demo_session(id),
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload_json TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

ALTER TABLE school_bus_attendance
    ADD CONSTRAINT fk_attendance_trip_execution
        FOREIGN KEY (trip_id) REFERENCES school_bus_trip_execution(id);

ALTER TABLE school_bus_attendance
    ADD CONSTRAINT fk_attendance_route_stop
        FOREIGN KEY (route_stop_id) REFERENCES school_bus_route_stop(id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_school_tenant_code
    ON school_bus_school (tenant_id, code)
    WHERE code IS NOT NULL AND is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_bus_tenant_plate
    ON school_bus_bus (tenant_id, plate_number)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_parent_tenant_user
    ON school_bus_parent_profile (tenant_id, user_id)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_driver_tenant_user
    ON school_bus_driver_profile (tenant_id, user_id)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_driver_tenant_license
    ON school_bus_driver_profile (tenant_id, license_number)
    WHERE license_number IS NOT NULL AND is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_attendant_tenant_user
    ON school_bus_attendant_profile (tenant_id, user_id)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_student_tenant_code
    ON school_bus_student (tenant_id, student_code)
    WHERE student_code IS NOT NULL AND is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_route_stop_order
    ON school_bus_route_stop (route_id, stop_order)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_subscription_code
    ON school_bus_student_subscription (tenant_id, subscription_code)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_trip_code
    ON school_bus_trip_execution (tenant_id, trip_code)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_trip_student
    ON school_bus_trip_student (trip_id, student_id)
    WHERE is_deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_bus_trip_stop_log
    ON school_bus_trip_stop_log (trip_id, route_stop_id)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_subscription_tenant_student_status
    ON school_bus_student_subscription (tenant_id, student_id, status, is_deleted);

CREATE INDEX IF NOT EXISTS idx_subscription_tenant_school_status
    ON school_bus_student_subscription (tenant_id, school_id, status, is_deleted);

CREATE INDEX IF NOT EXISTS idx_trip_execution_tenant_date_status
    ON school_bus_trip_execution (tenant_id, service_date, status, is_deleted);

CREATE INDEX IF NOT EXISTS idx_trip_stop_log_trip_order
    ON school_bus_trip_stop_log (trip_id, stop_order, is_deleted);

CREATE INDEX IF NOT EXISTS idx_trip_student_trip_status
    ON school_bus_trip_student (trip_id, status, is_deleted);

CREATE INDEX IF NOT EXISTS idx_demo_session_trip_status
    ON school_bus_demo_session (trip_id, status, is_deleted);

ALTER TABLE school_bus_bus
    ADD CONSTRAINT chk_school_bus_bus_capacity_positive
        CHECK (capacity > 0);

ALTER TABLE school_bus_school
    ADD CONSTRAINT chk_school_bus_school_lat_lng_pair
        CHECK ((latitude IS NULL AND longitude IS NULL) OR (latitude IS NOT NULL AND longitude IS NOT NULL));

ALTER TABLE school_bus_pickup_point
    ADD CONSTRAINT chk_school_bus_pickup_lat_lng_pair
        CHECK ((latitude IS NULL AND longitude IS NULL) OR (latitude IS NOT NULL AND longitude IS NOT NULL));

