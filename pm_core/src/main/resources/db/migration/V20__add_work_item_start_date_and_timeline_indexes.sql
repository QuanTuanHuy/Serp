ALTER TABLE work_items
    ADD COLUMN start_date TIMESTAMP;

CREATE INDEX idx_work_items_project_schedule
    ON work_items (tenant_id, project_id, start_date, due_date)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_work_items_project_parent_rank
    ON work_items (tenant_id, project_id, parent_id, rank)
    WHERE deleted_at IS NULL;
