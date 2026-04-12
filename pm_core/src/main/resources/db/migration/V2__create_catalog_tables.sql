CREATE TABLE status_categories (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    key VARCHAR(255) NOT NULL,
    color_name VARCHAR(255),
    is_system BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE statuses (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    status_key VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    category_id BIGINT,
    is_system BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_statuses_category
        FOREIGN KEY (category_id) REFERENCES status_categories (id)
);

CREATE TABLE issue_types (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    type_key VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    hierarchy_level INTEGER,
    is_system BOOLEAN,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE priorities (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    priority_key VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    color VARCHAR(255),
    sequence INTEGER,
    is_system BOOLEAN,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE custom_fields (
    id BIGSERIAL PRIMARY KEY,
    field_key VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type_key VARCHAR(255) NOT NULL,
    search_template VARCHAR(255),
    is_system BOOLEAN,
    schema_json TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE custom_field_contexts (
    id BIGSERIAL PRIMARY KEY,
    custom_field_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    issue_type_key VARCHAR(255),
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_custom_field_contexts_field
        FOREIGN KEY (custom_field_id) REFERENCES custom_fields (id)
);

CREATE TABLE custom_field_options (
    id BIGSERIAL PRIMARY KEY,
    custom_field_context_id BIGINT NOT NULL,
    option_key VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    sequence INTEGER,
    parent_option_id BIGINT,
    is_disabled BOOLEAN,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_custom_field_options_context
        FOREIGN KEY (custom_field_context_id) REFERENCES custom_field_contexts (id),
    CONSTRAINT fk_custom_field_options_parent
        FOREIGN KEY (parent_option_id) REFERENCES custom_field_options (id)
);

CREATE TABLE custom_field_context_default_values (
    id BIGSERIAL PRIMARY KEY,
    context_id BIGINT NOT NULL,
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
    CONSTRAINT fk_custom_field_context_default_values_context
        FOREIGN KEY (context_id) REFERENCES custom_field_contexts (id),
    CONSTRAINT fk_custom_field_context_default_values_option
        FOREIGN KEY (option_value_id) REFERENCES custom_field_options (id)
);

CREATE UNIQUE INDEX uk_status_categories_tenant_key
    ON status_categories (tenant_id, key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_statuses_tenant_status_key
    ON statuses (tenant_id, status_key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_statuses_tenant_category
    ON statuses (tenant_id, category_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_issue_types_tenant_type_key
    ON issue_types (tenant_id, type_key)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_types_tenant_hierarchy
    ON issue_types (tenant_id, hierarchy_level)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_priorities_tenant_priority_key
    ON priorities (tenant_id, priority_key)
    WHERE deleted_at IS NULL AND priority_key IS NOT NULL;

CREATE INDEX idx_priorities_tenant_sequence
    ON priorities (tenant_id, sequence)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_custom_fields_field_key
    ON custom_fields (field_key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_custom_field_contexts_exact
    ON custom_field_contexts (custom_field_id, issue_type_key)
    WHERE deleted_at IS NULL AND issue_type_key IS NOT NULL;

CREATE UNIQUE INDEX uk_custom_field_contexts_global
    ON custom_field_contexts (custom_field_id)
    WHERE deleted_at IS NULL AND issue_type_key IS NULL;

CREATE UNIQUE INDEX uk_custom_field_options_context_option_key
    ON custom_field_options (custom_field_context_id, option_key)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_custom_field_context_default_values_sort_order
    ON custom_field_context_default_values (context_id, sort_order)
    WHERE deleted_at IS NULL AND sort_order IS NOT NULL;
