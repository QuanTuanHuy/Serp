ALTER TABLE issue_link_types
    ADD COLUMN dependency_behavior VARCHAR(50) NOT NULL DEFAULT 'NONE';

UPDATE issue_link_types
SET dependency_behavior = 'SOURCE_BLOCKS_TARGET'
WHERE LOWER(name) = 'blocks'
  AND deleted_at IS NULL;

UPDATE issue_link_types
SET dependency_behavior = 'NONE'
WHERE LOWER(name) IN ('clones', 'relates')
  AND deleted_at IS NULL;

CREATE TABLE work_item_plans (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    planned_start TIMESTAMP NOT NULL,
    planned_end TIMESTAMP NOT NULL,
    source VARCHAR(50) NOT NULL,
    source_run_id BIGINT,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_plans_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id),
    CONSTRAINT chk_work_item_plans_range
        CHECK (planned_start < planned_end)
);

CREATE UNIQUE INDEX uk_work_item_plans_active_work_item
    ON work_item_plans (tenant_id, work_item_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_plans_project
    ON work_item_plans (tenant_id, project_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_plans_source_run
    ON work_item_plans (tenant_id, source_run_id)
    WHERE deleted_at IS NULL;

CREATE TABLE optimization_runs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    scope VARCHAR(50) NOT NULL,
    mode VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    planning_start TIMESTAMP NOT NULL,
    planning_end TIMESTAMP NOT NULL,
    allow_reassignment BOOLEAN NOT NULL,
    allow_schedule_changes BOOLEAN NOT NULL,
    selected_work_item_count INTEGER NOT NULL,
    summary_json TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    applied_at TIMESTAMP,
    applied_by BIGINT,
    discarded_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_optimization_runs_planning_range
        CHECK (planning_start < planning_end)
);

CREATE INDEX idx_optimization_runs_project
    ON optimization_runs (tenant_id, project_id, id DESC)
    WHERE deleted_at IS NULL;

CREATE TABLE optimization_run_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    work_item_updated_at_snapshot TIMESTAMP,
    plan_updated_at_snapshot TIMESTAMP,
    current_assignee_id BIGINT,
    suggested_assignee_id BIGINT,
    override_assignee_id BIGINT,
    current_planned_start TIMESTAMP,
    current_planned_end TIMESTAMP,
    suggested_planned_start TIMESTAMP,
    suggested_planned_end TIMESTAMP,
    override_planned_start TIMESTAMP,
    override_planned_end TIMESTAMP,
    current_due_date TIMESTAMP,
    assignment_decision VARCHAR(50) NOT NULL,
    schedule_decision VARCHAR(50) NOT NULL,
    assignment_apply_status VARCHAR(50) NOT NULL,
    schedule_apply_status VARCHAR(50) NOT NULL,
    score NUMERIC(18, 6),
    cost NUMERIC(18, 6),
    confidence VARCHAR(50),
    assignment_reasons_json TEXT,
    schedule_reasons_json TEXT,
    violations_json TEXT,
    applied_at TIMESTAMP,
    assignment_skipped_reason VARCHAR(255),
    schedule_skipped_reason VARCHAR(255),
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_optimization_run_items_run
        FOREIGN KEY (run_id) REFERENCES optimization_runs (id),
    CONSTRAINT fk_optimization_run_items_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
);

CREATE UNIQUE INDEX uk_optimization_run_items_active_work_item
    ON optimization_run_items (tenant_id, run_id, work_item_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_optimization_run_items_run
    ON optimization_run_items (tenant_id, run_id)
    WHERE deleted_at IS NULL;

CREATE TABLE optimization_run_warnings (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    work_item_id BIGINT,
    severity VARCHAR(50) NOT NULL,
    code VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    details_json TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_optimization_run_warnings_run
        FOREIGN KEY (run_id) REFERENCES optimization_runs (id),
    CONSTRAINT fk_optimization_run_warnings_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
);

CREATE INDEX idx_optimization_run_warnings_run
    ON optimization_run_warnings (tenant_id, run_id)
    WHERE deleted_at IS NULL;
