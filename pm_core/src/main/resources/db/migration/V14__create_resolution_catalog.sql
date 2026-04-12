CREATE TABLE resolutions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sequence INTEGER,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP
);

ALTER TABLE work_items
    ADD CONSTRAINT fk_work_items_resolution
        FOREIGN KEY (resolution_id) REFERENCES resolutions (id);

CREATE UNIQUE INDEX uk_resolutions_tenant_name
    ON resolutions (tenant_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_resolutions_tenant_sequence
    ON resolutions (tenant_id, sequence)
    WHERE deleted_at IS NULL;
