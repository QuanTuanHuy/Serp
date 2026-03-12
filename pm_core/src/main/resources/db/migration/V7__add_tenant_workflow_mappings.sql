-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Add per-tenant workflow materialization mappings

CREATE TABLE IF NOT EXISTS tenant_workflow_mappings (
    id                 BIGSERIAL PRIMARY KEY,
    tenant_id          BIGINT    NOT NULL,
    source_workflow_id BIGINT    NOT NULL,
    tenant_workflow_id BIGINT    NOT NULL,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         BIGINT,
    updated_by         BIGINT,
    deleted_at         TIMESTAMP NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_tenant_workflow_mappings_source
    ON tenant_workflow_mappings (tenant_id, source_workflow_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_tenant_workflow_mappings_target
    ON tenant_workflow_mappings (tenant_id, tenant_workflow_id)
    WHERE deleted_at IS NULL;
