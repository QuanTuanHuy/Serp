CREATE TABLE screens (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE screen_tabs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    screen_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_screen_tabs_screen
        FOREIGN KEY (screen_id) REFERENCES screens (id)
);

CREATE TABLE screen_tab_fields (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    screen_tab_id BIGINT NOT NULL,
    field_ref_type VARCHAR(20) NOT NULL,
    field_ref VARCHAR(255) NOT NULL,
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_screen_tab_fields_ref_type
        CHECK (field_ref_type IN ('SYSTEM', 'CUSTOM')),
    CONSTRAINT fk_screen_tab_fields_screen_tab
        FOREIGN KEY (screen_tab_id) REFERENCES screen_tabs (id)
);

CREATE TABLE screen_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_screen_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_screen_schemes_default_screen
        FOREIGN KEY (default_screen_id) REFERENCES screens (id)
);

CREATE TABLE screen_scheme_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    screen_scheme_id BIGINT NOT NULL,
    operation_key VARCHAR(30) NOT NULL,
    screen_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_screen_scheme_items_operation
        CHECK (operation_key IN ('CREATE', 'EDIT', 'VIEW')),
    CONSTRAINT fk_screen_scheme_items_screen_scheme
        FOREIGN KEY (screen_scheme_id) REFERENCES screen_schemes (id),
    CONSTRAINT fk_screen_scheme_items_screen
        FOREIGN KEY (screen_id) REFERENCES screens (id)
);

CREATE TABLE issue_type_screen_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_screen_scheme_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_issue_type_screen_schemes_default_screen_scheme
        FOREIGN KEY (default_screen_scheme_id) REFERENCES screen_schemes (id)
);

CREATE TABLE issue_type_screen_scheme_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    screen_scheme_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_issue_type_screen_scheme_items_scheme
        FOREIGN KEY (scheme_id) REFERENCES issue_type_screen_schemes (id),
    CONSTRAINT fk_issue_type_screen_scheme_items_issue_type
        FOREIGN KEY (issue_type_id) REFERENCES issue_types (id),
    CONSTRAINT fk_issue_type_screen_scheme_items_screen_scheme
        FOREIGN KEY (screen_scheme_id) REFERENCES screen_schemes (id)
);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_issue_type_screen_scheme
        FOREIGN KEY (issue_type_screen_scheme_id) REFERENCES issue_type_screen_schemes (id);

CREATE INDEX idx_screens_tenant_name
    ON screens (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_screen_tabs_screen_sequence
    ON screen_tabs (tenant_id, screen_id, sequence)
    WHERE deleted_at IS NULL AND sequence IS NOT NULL;

CREATE UNIQUE INDEX uk_screen_tab_fields_tab_sequence
    ON screen_tab_fields (tenant_id, screen_tab_id, sequence)
    WHERE deleted_at IS NULL AND sequence IS NOT NULL;

CREATE INDEX idx_screen_schemes_tenant_name
    ON screen_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_screen_scheme_items_operation
    ON screen_scheme_items (tenant_id, screen_scheme_id, operation_key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_type_screen_schemes_tenant_name
    ON issue_type_screen_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_issue_type_screen_scheme_items_scheme_issue_type
    ON issue_type_screen_scheme_items (tenant_id, scheme_id, issue_type_id)
    WHERE deleted_at IS NULL;
