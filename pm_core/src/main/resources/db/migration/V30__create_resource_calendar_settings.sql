CREATE TABLE resource_calendar_profiles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    timezone VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_resource_calendar_profiles_tenant
    ON resource_calendar_profiles (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE TABLE resource_calendar_profile_blocks (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity_factor NUMERIC(5, 2) NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_resource_calendar_profile_blocks_profile
        FOREIGN KEY (profile_id) REFERENCES resource_calendar_profiles (id),
    CONSTRAINT chk_resource_calendar_profile_blocks_day
        CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_resource_calendar_profile_blocks_range
        CHECK (start_time < end_time),
    CONSTRAINT chk_resource_calendar_profile_blocks_capacity
        CHECK (capacity_factor > 0 AND capacity_factor <= 1)
);

CREATE INDEX idx_resource_calendar_profile_blocks_profile
    ON resource_calendar_profile_blocks (profile_id, day_of_week, start_time)
    WHERE deleted_at IS NULL;

CREATE TABLE resource_calendar_assignments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_resource_calendar_assignments_profile
        FOREIGN KEY (profile_id) REFERENCES resource_calendar_profiles (id),
    CONSTRAINT chk_resource_calendar_assignments_range
        CHECK (effective_to IS NULL OR effective_from <= effective_to)
);

CREATE INDEX idx_resource_calendar_assignments_user
    ON resource_calendar_assignments (tenant_id, user_id, effective_from, effective_to)
    WHERE deleted_at IS NULL;

CREATE TABLE resource_calendar_exceptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    exception_type VARCHAR(50) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    capacity_factor NUMERIC(5, 2),
    reason VARCHAR(500),
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_resource_calendar_exceptions_range
        CHECK (start_at < end_at),
    CONSTRAINT chk_resource_calendar_exceptions_type
        CHECK (exception_type IN ('UNAVAILABLE', 'CAPACITY_OVERRIDE')),
    CONSTRAINT chk_resource_calendar_exceptions_capacity
        CHECK (capacity_factor IS NULL OR (capacity_factor >= 0 AND capacity_factor <= 2))
);

CREATE INDEX idx_resource_calendar_exceptions_user_range
    ON resource_calendar_exceptions (tenant_id, user_id, start_at, end_at)
    WHERE deleted_at IS NULL;
