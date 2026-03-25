DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'work_items'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'custom_fields'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'custom_field_contexts'
    ) THEN
        CREATE TABLE IF NOT EXISTS work_item_custom_field_values (
            id BIGSERIAL PRIMARY KEY,
            tenant_id BIGINT NOT NULL,
            work_item_id BIGINT NOT NULL,
            custom_field_id BIGINT NOT NULL,
            custom_field_context_id BIGINT NOT NULL,
            value_type VARCHAR(30) NOT NULL,
            text_value TEXT,
            number_value NUMERIC(20, 6),
            date_value DATE,
            datetime_value TIMESTAMP,
            user_value_id BIGINT,
            group_value_id VARCHAR(255),
            option_value_id BIGINT,
            json_value JSONB,
            sort_order INT,
            created_at TIMESTAMP,
            created_by BIGINT,
            updated_at TIMESTAMP,
            updated_by BIGINT,
            deleted_at TIMESTAMP
        );

        IF NOT EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE schemaname = current_schema()
              AND indexname = 'uk_work_item_custom_field_values_unique'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uk_work_item_custom_field_values_unique ON work_item_custom_field_values (tenant_id, work_item_id, custom_field_id, custom_field_context_id, sort_order) WHERE deleted_at IS NULL';
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_indexes
            WHERE schemaname = current_schema()
              AND indexname = 'idx_work_item_custom_field_values_lookup'
        ) THEN
            EXECUTE 'CREATE INDEX idx_work_item_custom_field_values_lookup ON work_item_custom_field_values (tenant_id, work_item_id, custom_field_id, custom_field_context_id) WHERE deleted_at IS NULL';
        END IF;
    END IF;
END $$;
