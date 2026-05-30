CREATE TABLE resource_calendar_slots (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    slot_start TIMESTAMP NOT NULL,
    slot_end TIMESTAMP NOT NULL,
    capacity_millis BIGINT NOT NULL,
    source VARCHAR(50) NOT NULL,
    external_ref VARCHAR(255),
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_resource_calendar_slots_range
        CHECK (slot_start < slot_end),
    CONSTRAINT chk_resource_calendar_slots_capacity
        CHECK (capacity_millis > 0)
);

CREATE INDEX idx_resource_calendar_slots_user_range
    ON resource_calendar_slots (tenant_id, user_id, slot_start, slot_end)
    WHERE deleted_at IS NULL;

ALTER TABLE optimization_run_items
    ADD COLUMN allocation_chunks_json TEXT;
