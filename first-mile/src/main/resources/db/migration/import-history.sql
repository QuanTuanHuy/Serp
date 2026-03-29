CREATE TABLE import_history (
                             id BIGSERIAL PRIMARY KEY,
                             file_id UUID NOT NULL UNIQUE,
                             file_name VARCHAR(255),
                             status VARCHAR(64) NOT NULL,
                             total_records INT,
                             success_records INT,
                             failed_records INT,
                             error_message TEXT,
                             started_at TIMESTAMP,
                             finished_at TIMESTAMP,

    -- Audit fields
                             created_at TIMESTAMP,
                             updated_at TIMESTAMP,
                             created_by VARCHAR(255),
                             updated_by VARCHAR(255),
                             tenant_id BIGINT
);

CREATE INDEX idx_import_history_tenant_id ON import_history(tenant_id);
CREATE INDEX idx_import_history_status ON import_history(status);
CREATE INDEX idx_import_history_created_at ON import_history(created_at DESC);
