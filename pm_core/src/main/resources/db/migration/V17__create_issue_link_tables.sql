CREATE TABLE issue_link_types (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    outward_desc VARCHAR(100) NOT NULL,
    inward_desc VARCHAR(100) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

CREATE TABLE issue_links (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    source_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    link_type_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_issue_links_source
        FOREIGN KEY (source_id) REFERENCES work_items (id),
    CONSTRAINT fk_issue_links_target
        FOREIGN KEY (target_id) REFERENCES work_items (id),
    CONSTRAINT fk_issue_links_link_type
        FOREIGN KEY (link_type_id) REFERENCES issue_link_types (id),
    CONSTRAINT chk_issue_links_not_self
        CHECK (source_id <> target_id)
);

CREATE UNIQUE INDEX uk_issue_link_types_tenant_name
    ON issue_link_types (tenant_id, LOWER(name))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_issue_links_unique_active
    ON issue_links (tenant_id, source_id, target_id, link_type_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_links_source_lookup
    ON issue_links (tenant_id, source_id, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_links_target_lookup
    ON issue_links (tenant_id, target_id, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_issue_links_link_type_lookup
    ON issue_links (tenant_id, link_type_id)
    WHERE deleted_at IS NULL;
