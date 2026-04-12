CREATE TABLE issue_type_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_issue_type_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_issue_type_schemes_default_issue_type
        FOREIGN KEY (default_issue_type_id) REFERENCES issue_types (id)
);

CREATE TABLE issue_type_scheme_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_issue_type_scheme_items_scheme
        FOREIGN KEY (scheme_id) REFERENCES issue_type_schemes (id),
    CONSTRAINT fk_issue_type_scheme_items_issue_type
        FOREIGN KEY (issue_type_id) REFERENCES issue_types (id)
);

CREATE TABLE priority_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_priority_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_priority_schemes_default_priority
        FOREIGN KEY (default_priority_id) REFERENCES priorities (id)
);

CREATE TABLE priority_scheme_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    priority_id BIGINT NOT NULL,
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_priority_scheme_items_scheme
        FOREIGN KEY (scheme_id) REFERENCES priority_schemes (id),
    CONSTRAINT fk_priority_scheme_items_priority
        FOREIGN KEY (priority_id) REFERENCES priorities (id)
);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_issue_type_scheme
        FOREIGN KEY (issue_type_scheme_id) REFERENCES issue_type_schemes (id);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_priority_scheme
        FOREIGN KEY (priority_scheme_id) REFERENCES priority_schemes (id);

CREATE INDEX idx_issue_type_schemes_tenant_name
    ON issue_type_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_issue_type_scheme_items_scheme_issue_type
    ON issue_type_scheme_items (tenant_id, scheme_id, issue_type_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_type_scheme_items_scheme
    ON issue_type_scheme_items (tenant_id, scheme_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_priority_schemes_tenant_name
    ON priority_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_priority_scheme_items_scheme_priority
    ON priority_scheme_items (tenant_id, scheme_id, priority_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_priority_scheme_items_scheme
    ON priority_scheme_items (tenant_id, scheme_id)
    WHERE deleted_at IS NULL;
