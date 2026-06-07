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

CREATE TABLE work_item_plan_allocations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    work_item_plan_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    effort_millis BIGINT NOT NULL,
    source VARCHAR(50) NOT NULL,
    source_run_id BIGINT,
    source_run_item_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_plan_allocations_plan
        FOREIGN KEY (work_item_plan_id) REFERENCES work_item_plans (id),
    CONSTRAINT fk_work_item_plan_allocations_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id),
    CONSTRAINT chk_work_item_plan_allocations_range
        CHECK (start_time < end_time),
    CONSTRAINT chk_work_item_plan_allocations_effort
        CHECK (effort_millis > 0)
);

CREATE INDEX idx_work_item_plan_allocations_plan
    ON work_item_plan_allocations (tenant_id, work_item_plan_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_plan_allocations_work_item
    ON work_item_plan_allocations (tenant_id, work_item_id, start_time)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_plan_allocations_assignee_range
    ON work_item_plan_allocations (tenant_id, assignee_id, start_time, end_time)
    WHERE deleted_at IS NULL;
