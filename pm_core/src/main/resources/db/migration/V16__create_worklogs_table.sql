CREATE TABLE worklogs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    comment TEXT,
    start_date TIMESTAMP NOT NULL,
    time_spent BIGINT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_worklogs_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id),
    CONSTRAINT chk_worklogs_time_spent_positive
        CHECK (time_spent >= 0)
);

CREATE INDEX idx_worklogs_work_item_start_date
    ON worklogs (tenant_id, work_item_id, start_date DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_worklogs_work_item_author
    ON worklogs (tenant_id, work_item_id, author_id)
    WHERE deleted_at IS NULL;
