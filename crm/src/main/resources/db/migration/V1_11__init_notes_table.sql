-- Author: QuanTuanHuy
-- Description: Part of Serp Project

CREATE TABLE IF NOT EXISTS notes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT chk_notes_entity_type 
        CHECK (entity_type IN ('LEAD', 'ACCOUNT', 'OPPORTUNITY', 'ACTIVITY'))
);

CREATE INDEX IF NOT EXISTS idx_notes_tenant_entity 
    ON notes (tenant_id, entity_type, entity_id);
