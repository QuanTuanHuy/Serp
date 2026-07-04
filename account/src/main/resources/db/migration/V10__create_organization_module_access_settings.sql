-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Purpose: Store organization-module module access settings

CREATE TABLE organization_module_access_settings (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    auto_grant_to_new_users BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_org_module_access_settings UNIQUE (organization_id, module_id)
);

CREATE INDEX idx_org_module_access_settings_org
    ON organization_module_access_settings(organization_id);

CREATE INDEX idx_org_module_access_settings_org_auto_grant
    ON organization_module_access_settings(organization_id, auto_grant_to_new_users);

COMMENT ON TABLE organization_module_access_settings
    IS 'Organization-level module access settings such as auto-grant to new users';

COMMENT ON COLUMN organization_module_access_settings.auto_grant_to_new_users
    IS 'When true, new users in the organization are automatically granted access to this module';
