CREATE TABLE work_item_history (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    field_key VARCHAR(100) NOT NULL,
    field_name VARCHAR(255),
    from_value TEXT,
    to_value TEXT,
    from_display_value TEXT,
    to_display_value TEXT,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_history_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
);

CREATE INDEX idx_work_item_history_work_item_created
    ON work_item_history (tenant_id, work_item_id, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_history_field
    ON work_item_history (tenant_id, work_item_id, field_key)
    WHERE deleted_at IS NULL;
