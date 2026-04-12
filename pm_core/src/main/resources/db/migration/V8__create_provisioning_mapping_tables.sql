CREATE TABLE tenant_scheme_defaults (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_type VARCHAR(50) NOT NULL,
    scheme_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_tenant_scheme_defaults_scheme_type
        CHECK (
            scheme_type IN (
                'ISSUE_TYPE',
                'PRIORITY',
                'WORKFLOW',
                'FIELD_CONFIG',
                'SCREEN',
                'PERMISSION',
                'ISSUE_SECURITY',
                'NOTIFICATION'
            )
        )
);

CREATE TABLE tenant_scheme_mappings (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scheme_type VARCHAR(50) NOT NULL,
    source_scheme_id BIGINT NOT NULL,
    tenant_scheme_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_tenant_scheme_mappings_scheme_type
        CHECK (
            scheme_type IN (
                'ISSUE_TYPE',
                'PRIORITY',
                'WORKFLOW',
                'FIELD_CONFIG',
                'SCREEN',
                'PERMISSION',
                'ISSUE_SECURITY',
                'NOTIFICATION'
            )
        )
);

CREATE TABLE blueprint_scheme_defaults (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    blueprint_id BIGINT NOT NULL,
    scheme_type VARCHAR(50) NOT NULL,
    scheme_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_blueprint_scheme_defaults_scheme_type
        CHECK (
            scheme_type IN (
                'ISSUE_TYPE',
                'PRIORITY',
                'WORKFLOW',
                'FIELD_CONFIG',
                'SCREEN',
                'PERMISSION',
                'ISSUE_SECURITY',
                'NOTIFICATION'
            )
        ),
    CONSTRAINT fk_blueprint_scheme_defaults_blueprint
        FOREIGN KEY (blueprint_id) REFERENCES project_blueprints (id)
);

CREATE UNIQUE INDEX uk_tenant_scheme_defaults_scheme_type
    ON tenant_scheme_defaults (tenant_id, scheme_type)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_tenant_scheme_mappings_source_scheme
    ON tenant_scheme_mappings (tenant_id, scheme_type, source_scheme_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_blueprint_scheme_defaults_scheme_type
    ON blueprint_scheme_defaults (tenant_id, blueprint_id, scheme_type)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_blueprint_scheme_defaults_blueprint
    ON blueprint_scheme_defaults (tenant_id, blueprint_id)
    WHERE deleted_at IS NULL;
