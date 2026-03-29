CREATE TABLE field_configurations (
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

CREATE TABLE field_configuration_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    field_configuration_id BIGINT NOT NULL,
    field_ref_type VARCHAR(20) NOT NULL,
    field_ref VARCHAR(255) NOT NULL,
    is_required BOOLEAN,
    is_hidden BOOLEAN,
    renderer_key VARCHAR(255),
    sequence INTEGER,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_field_configuration_items_ref_type
        CHECK (field_ref_type IN ('SYSTEM', 'CUSTOM')),
    CONSTRAINT fk_field_configuration_items_field_configuration
        FOREIGN KEY (field_configuration_id) REFERENCES field_configurations (id)
);

CREATE TABLE field_config_schemes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_field_config_id BIGINT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_field_config_schemes_default_field_configuration
        FOREIGN KEY (default_field_config_id) REFERENCES field_configurations (id)
);

CREATE TABLE field_config_scheme_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_id BIGINT NOT NULL,
    issue_type_id BIGINT NOT NULL,
    field_configuration_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_field_config_scheme_items_scheme
        FOREIGN KEY (scheme_id) REFERENCES field_config_schemes (id),
    CONSTRAINT fk_field_config_scheme_items_issue_type
        FOREIGN KEY (issue_type_id) REFERENCES issue_types (id),
    CONSTRAINT fk_field_config_scheme_items_field_configuration
        FOREIGN KEY (field_configuration_id) REFERENCES field_configurations (id)
);

ALTER TABLE projects
    ADD CONSTRAINT fk_projects_field_config_scheme
        FOREIGN KEY (field_config_scheme_id) REFERENCES field_config_schemes (id);

CREATE INDEX idx_field_configurations_tenant_name
    ON field_configurations (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_field_configuration_items_field_ref
    ON field_configuration_items (tenant_id, field_configuration_id, field_ref_type, field_ref)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_field_config_schemes_tenant_name
    ON field_config_schemes (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_field_config_scheme_items_scheme_issue_type
    ON field_config_scheme_items (tenant_id, scheme_id, issue_type_id)
    WHERE deleted_at IS NULL;
