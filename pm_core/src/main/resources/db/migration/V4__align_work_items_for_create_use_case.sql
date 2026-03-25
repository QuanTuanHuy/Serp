DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'work_items'
    ) THEN
        ALTER TABLE work_items ADD COLUMN IF NOT EXISTS workflow_step_id BIGINT;
        ALTER TABLE work_items ADD COLUMN IF NOT EXISTS security_level_id BIGINT;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE schemaname = current_schema()
              AND indexname = 'uk_work_items_tenant_key'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uk_work_items_tenant_key ON work_items (tenant_id, key) WHERE deleted_at IS NULL';
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE schemaname = current_schema()
              AND indexname = 'uk_work_items_tenant_project_issue_no'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uk_work_items_tenant_project_issue_no ON work_items (tenant_id, project_id, issue_no) WHERE deleted_at IS NULL';
        END IF;
    END IF;
END $$;
