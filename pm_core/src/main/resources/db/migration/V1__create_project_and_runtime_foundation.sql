CREATE TABLE project_categories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system BOOLEAN,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE project_blueprints (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    project_type_key VARCHAR(50),
    avatar_url VARCHAR(255),
    is_system BOOLEAN,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_project_blueprints_project_type
        CHECK (project_type_key IS NULL OR project_type_key IN ('software', 'business'))
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    key VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    url VARCHAR(255),
    lead_user_id BIGINT,
    avatar_id BIGINT,
    project_category_id BIGINT,
    project_type_key VARCHAR(50),
    archived BOOLEAN,
    archived_at TIMESTAMP,
    issue_type_scheme_id BIGINT,
    workflow_scheme_id BIGINT,
    field_config_scheme_id BIGINT,
    issue_type_screen_scheme_id BIGINT,
    permission_scheme_id BIGINT,
    notification_scheme_id BIGINT,
    priority_scheme_id BIGINT,
    issue_security_scheme_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_projects_project_type
        CHECK (project_type_key IS NULL OR project_type_key IN ('software', 'business')),
    CONSTRAINT fk_projects_project_category
        FOREIGN KEY (project_category_id) REFERENCES project_categories (id)
);

CREATE TABLE project_roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE project_role_actors (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_role_id BIGINT NOT NULL,
    subject_type VARCHAR(20) NOT NULL,
    subject_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_project_role_actors_subject_type
        CHECK (subject_type IN ('USER', 'GROUP', 'SERVICE_ACCOUNT')),
    CONSTRAINT fk_project_role_actors_project
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_role_actors_role
        FOREIGN KEY (project_role_id) REFERENCES project_roles (id)
);

CREATE TABLE labels (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(255),
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_labels_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    partition_key VARCHAR(255),
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP,
    published_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED', 'DEAD'))
);

CREATE TABLE project_issue_counters (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    counter BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_project_issue_counters_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE consumer_inbox_events (
    id BIGSERIAL PRIMARY KEY,
    consumer_group VARCHAR(255) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    partition_no INTEGER,
    offset_no BIGINT,
    tenant_id BIGINT,
    payload_hash VARCHAR(255),
    raw_payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 1,
    last_error TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_consumer_inbox_events_consumer_event
        UNIQUE (consumer_group, event_id),
    CONSTRAINT chk_consumer_inbox_events_status
        CHECK (status IN ('PROCESSING', 'PROCESSED', 'FAILED', 'DEAD'))
);

CREATE UNIQUE INDEX uk_project_categories_tenant_name
    ON project_categories (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_blueprints_tenant_type
    ON project_blueprints (tenant_id, project_type_key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_projects_tenant_key
    ON projects (tenant_id, key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_projects_tenant_name
    ON projects (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_project_roles_tenant_name
    ON project_roles (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_role_actors_project_role
    ON project_role_actors (tenant_id, project_id, project_role_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_project_role_actors_subject
    ON project_role_actors (tenant_id, subject_type, subject_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_project_role_actors_assignment
    ON project_role_actors (tenant_id, project_id, project_role_id, subject_type, subject_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_labels_project_name
    ON labels (tenant_id, project_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_outbox_events_status_retry
    ON outbox_events (status, next_retry_at, created_at);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (tenant_id, aggregate_type, aggregate_id);

CREATE UNIQUE INDEX uk_project_issue_counters_project
    ON project_issue_counters (tenant_id, project_id);

CREATE INDEX idx_consumer_inbox_events_status
    ON consumer_inbox_events (status, updated_at);
