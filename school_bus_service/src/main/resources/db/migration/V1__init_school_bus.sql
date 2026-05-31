CREATE TABLE school_bus_school (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    address TEXT,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE school_bus_parent_profile (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE school_bus_bus (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    plate_number VARCHAR(100) NOT NULL,
    bus_type VARCHAR(100),
    capacity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE school_bus_driver_profile (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    license_number VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE school_bus_attendant_profile (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE school_bus_pickup_point (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    school_id BIGINT NOT NULL REFERENCES school_bus_school(id),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    pickup_window_start TIME,
    pickup_window_end TIME,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE school_bus_student (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    school_id BIGINT NOT NULL REFERENCES school_bus_school(id),
    parent_profile_id BIGINT NOT NULL REFERENCES school_bus_parent_profile(id),
    pickup_point_id BIGINT REFERENCES school_bus_pickup_point(id),
    full_name VARCHAR(255) NOT NULL,
    student_code VARCHAR(100),
    grade VARCHAR(50),
    home_address TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE school_bus_transport_request (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    parent_profile_id BIGINT NOT NULL REFERENCES school_bus_parent_profile(id),
    school_id BIGINT NOT NULL REFERENCES school_bus_school(id),
    request_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    notes TEXT,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT
);

CREATE TABLE school_bus_request_student (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES school_bus_transport_request(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES school_bus_student(id),
    pickup_point_id BIGINT REFERENCES school_bus_pickup_point(id)
);

CREATE TABLE school_bus_route_plan (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    school_id BIGINT NOT NULL REFERENCES school_bus_school(id),
    route_code VARCHAR(100) NOT NULL,
    route_name VARCHAR(255) NOT NULL,
    service_date DATE NOT NULL,
    shift_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    planned_distance_km DOUBLE PRECISION,
    planned_duration_min INTEGER,
    planning_notes TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE school_bus_route_stop (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES school_bus_route_plan(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL,
    pickup_point_id BIGINT NOT NULL REFERENCES school_bus_pickup_point(id),
    stop_order INTEGER NOT NULL,
    estimated_student_count INTEGER NOT NULL DEFAULT 0,
    planned_arrival_time TIME,
    planned_departure_time TIME
);

CREATE TABLE school_bus_route_assignment (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES school_bus_route_plan(id) ON DELETE CASCADE,
    tenant_id BIGINT NOT NULL,
    bus_id BIGINT NOT NULL REFERENCES school_bus_bus(id),
    driver_id BIGINT NOT NULL REFERENCES school_bus_driver_profile(id),
    attendant_id BIGINT REFERENCES school_bus_attendant_profile(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE school_bus_attendance (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    route_id BIGINT NOT NULL REFERENCES school_bus_route_plan(id),
    student_id BIGINT NOT NULL REFERENCES school_bus_student(id),
    attendance_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    recorded_by BIGINT NOT NULL,
    notes TEXT
);

CREATE TABLE school_bus_trip_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    route_id BIGINT NOT NULL UNIQUE REFERENCES school_bus_route_plan(id),
    route_code VARCHAR(100) NOT NULL,
    service_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    bus_id BIGINT REFERENCES school_bus_bus(id),
    driver_id BIGINT REFERENCES school_bus_driver_profile(id),
    attendant_id BIGINT REFERENCES school_bus_attendant_profile(id)
);

CREATE TABLE school_bus_audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    created_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_stamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id BIGINT,
    action_type VARCHAR(100) NOT NULL,
    action_detail TEXT NOT NULL,
    performed_by BIGINT NOT NULL,
    performed_at TIMESTAMP NOT NULL
);
