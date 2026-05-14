CREATE TABLE project_components (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    lead_user_id BIGINT,
    assignee_type VARCHAR(30) NOT NULL DEFAULT 'PROJECT_DEFAULT',
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_project_components_assignee_type
        CHECK (assignee_type IN ('PROJECT_DEFAULT', 'COMPONENT_LEAD', 'PROJECT_LEAD', 'UNASSIGNED')),
    CONSTRAINT fk_project_components_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE work_item_components (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_components_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id),
    CONSTRAINT fk_work_item_components_component
        FOREIGN KEY (component_id) REFERENCES project_components (id)
);

CREATE UNIQUE INDEX uk_project_components_project_name
    ON project_components (tenant_id, project_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_components_project_lookup
    ON project_components (tenant_id, project_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_work_item_components_unique
    ON work_item_components (tenant_id, work_item_id, component_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_components_component
    ON work_item_components (tenant_id, component_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_components_work_item
    ON work_item_components (tenant_id, work_item_id)
    WHERE deleted_at IS NULL;
