CREATE TABLE work_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    issue_no BIGINT NOT NULL,
    key VARCHAR(30) NOT NULL,
    summary VARCHAR(512) NOT NULL,
    description TEXT,
    workflow_step_id BIGINT,
    status_id BIGINT NOT NULL,
    priority_id BIGINT NOT NULL,
    assignee_id BIGINT,
    reporter_id BIGINT NOT NULL,
    parent_id BIGINT,
    security_level_id BIGINT,
    due_date TIMESTAMP,
    rank VARCHAR(255),
    resolution_id BIGINT,
    time_original_estimate BIGINT,
    time_remaining_estimate BIGINT,
    time_spent BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_items_project
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_work_items_issue_type
        FOREIGN KEY (issue_type_id) REFERENCES issue_types (id),
    CONSTRAINT fk_work_items_workflow_step
        FOREIGN KEY (workflow_step_id) REFERENCES workflow_steps (id),
    CONSTRAINT fk_work_items_status
        FOREIGN KEY (status_id) REFERENCES statuses (id),
    CONSTRAINT fk_work_items_priority
        FOREIGN KEY (priority_id) REFERENCES priorities (id),
    CONSTRAINT fk_work_items_parent
        FOREIGN KEY (parent_id) REFERENCES work_items (id),
    CONSTRAINT fk_work_items_security_level
        FOREIGN KEY (security_level_id) REFERENCES issue_security_levels (id)
);

CREATE TABLE work_item_custom_field_values (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    custom_field_id BIGINT NOT NULL,
    custom_field_context_id BIGINT NOT NULL,
    value_type VARCHAR(30) NOT NULL,
    text_value TEXT,
    number_value NUMERIC(20, 6),
    date_value DATE,
    datetime_value TIMESTAMP,
    user_value_id BIGINT,
    group_value_id VARCHAR(255),
    option_value_id BIGINT,
    json_value TEXT,
    sort_order INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_work_item_custom_field_values_type
        CHECK (value_type IN ('TEXT', 'NUMBER', 'DATE', 'DATETIME', 'USER', 'GROUP', 'OPTION', 'JSON')),
    CONSTRAINT fk_work_item_custom_field_values_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id),
    CONSTRAINT fk_work_item_custom_field_values_custom_field
        FOREIGN KEY (custom_field_id) REFERENCES custom_fields (id),
    CONSTRAINT fk_work_item_custom_field_values_context
        FOREIGN KEY (custom_field_context_id) REFERENCES custom_field_contexts (id),
    CONSTRAINT fk_work_item_custom_field_values_option
        FOREIGN KEY (option_value_id) REFERENCES custom_field_options (id)
);

CREATE UNIQUE INDEX uk_work_items_tenant_key
    ON work_items (tenant_id, key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_work_items_tenant_project_issue_no
    ON work_items (tenant_id, project_id, issue_no)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_items_project_status_rank
    ON work_items (tenant_id, project_id, status_id, workflow_step_id, assignee_id, rank)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_work_item_custom_field_values_unique
    ON work_item_custom_field_values (
        tenant_id,
        work_item_id,
        custom_field_id,
        custom_field_context_id,
        COALESCE(sort_order, 0)
    )
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_custom_field_values_lookup
    ON work_item_custom_field_values (tenant_id, work_item_id, custom_field_id, custom_field_context_id)
    WHERE deleted_at IS NULL;
