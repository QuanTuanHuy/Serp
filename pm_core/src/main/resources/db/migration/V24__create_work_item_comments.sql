CREATE TABLE work_item_comments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    work_item_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_work_item_comments_work_item
        FOREIGN KEY (work_item_id) REFERENCES work_items (id)
);

CREATE INDEX idx_work_item_comments_work_item_created
    ON work_item_comments (tenant_id, work_item_id, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_item_comments_author
    ON work_item_comments (tenant_id, author_id)
    WHERE deleted_at IS NULL;
