CREATE TABLE workflows (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    workflow_key VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    current_published_version_id BIGINT,
    draft_version_id BIGINT,
    lifecycle_state VARCHAR(20) NOT NULL,
    is_system BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_workflows_lifecycle_state
        CHECK (lifecycle_state IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

CREATE TABLE workflow_versions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    workflow_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    version_state VARCHAR(20) NOT NULL,
    base_version_id BIGINT,
    published_at TIMESTAMP,
    published_by BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_workflow_versions_state
        CHECK (version_state IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_workflow_versions_workflow
        FOREIGN KEY (workflow_id) REFERENCES workflows (id),
    CONSTRAINT fk_workflow_versions_base_version
        FOREIGN KEY (base_version_id) REFERENCES workflow_versions (id)
);

CREATE TABLE workflow_steps (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    workflow_version_id BIGINT NOT NULL,
    step_key VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    status_id BIGINT NOT NULL,
    step_order INTEGER,
    is_initial BOOLEAN NOT NULL,
    is_terminal BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_workflow_steps_workflow_version
        FOREIGN KEY (workflow_version_id) REFERENCES workflow_versions (id),
    CONSTRAINT fk_workflow_steps_status
        FOREIGN KEY (status_id) REFERENCES statuses (id)
);

CREATE TABLE workflow_transitions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    workflow_version_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    from_step_id BIGINT,
    to_step_id BIGINT NOT NULL,
    screen_id BIGINT,
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_workflow_transitions_workflow_version
        FOREIGN KEY (workflow_version_id) REFERENCES workflow_versions (id),
    CONSTRAINT fk_workflow_transitions_from_step
        FOREIGN KEY (from_step_id) REFERENCES workflow_steps (id),
    CONSTRAINT fk_workflow_transitions_to_step
        FOREIGN KEY (to_step_id) REFERENCES workflow_steps (id),
    CONSTRAINT fk_workflow_transitions_screen
        FOREIGN KEY (screen_id) REFERENCES screens (id)
);

CREATE TABLE workflow_transition_rules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    transition_id BIGINT NOT NULL,
    rule_stage VARCHAR(20) NOT NULL,
    rule_key VARCHAR(255) NOT NULL,
    config_json TEXT,
    sequence INTEGER,
    is_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_workflow_transition_rules_stage
        CHECK (rule_stage IN ('CONDITION', 'VALIDATOR', 'POST_FUNCTION')),
    CONSTRAINT fk_workflow_transition_rules_transition
        FOREIGN KEY (transition_id) REFERENCES workflow_transitions (id)
);

CREATE TABLE workflow_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_workflow_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_workflow_schemes_default_workflow
        FOREIGN KEY (default_workflow_id) REFERENCES workflows (id)
);

CREATE TABLE workflow_scheme_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    workflow_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_workflow_scheme_items_scheme
        FOREIGN KEY (scheme_id) REFERENCES workflow_schemes (id),
    CONSTRAINT fk_workflow_scheme_items_issue_type
        FOREIGN KEY (issue_type_id) REFERENCES issue_types (id),
    CONSTRAINT fk_workflow_scheme_items_workflow
        FOREIGN KEY (workflow_id) REFERENCES workflows (id)
);

ALTER TABLE workflows
    ADD CONSTRAINT fk_workflows_current_published_version
        FOREIGN KEY (current_published_version_id) REFERENCES workflow_versions (id);

ALTER TABLE workflows
    ADD CONSTRAINT fk_workflows_draft_version
        FOREIGN KEY (draft_version_id) REFERENCES workflow_versions (id);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_workflow_scheme
        FOREIGN KEY (workflow_scheme_id) REFERENCES workflow_schemes (id);

CREATE UNIQUE INDEX uk_workflows_tenant_workflow_key
    ON workflows (tenant_id, workflow_key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_workflow_versions_workflow_version_no
    ON workflow_versions (tenant_id, workflow_id, version_no)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_workflow_steps_step_key
    ON workflow_steps (tenant_id, workflow_version_id, step_key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_workflow_steps_version_status
    ON workflow_steps (tenant_id, workflow_version_id, status_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_workflow_transitions_version_route
    ON workflow_transitions (tenant_id, workflow_version_id, from_step_id, to_step_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_workflow_transition_rules_transition
    ON workflow_transition_rules (tenant_id, transition_id, rule_stage)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_workflow_scheme_items_scheme_issue_type
    ON workflow_scheme_items (tenant_id, scheme_id, issue_type_id)
    WHERE deleted_at IS NULL;
