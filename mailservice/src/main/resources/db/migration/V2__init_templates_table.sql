/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

-- ======================================
-- V2: Create email_templates table
-- ======================================

CREATE TABLE IF NOT EXISTS email_templates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,
    
    subject VARCHAR(500) NOT NULL,
    body_template TEXT NOT NULL,
    is_html BOOLEAN DEFAULT true,
    
    variables_schema JSONB,
    default_values JSONB,
    
    type VARCHAR(50) NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    category VARCHAR(100),
    
    is_global BOOLEAN DEFAULT false,
    
    version INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT true,
    
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    CONSTRAINT chk_template_active_status CHECK (active_status IN ('ACTIVE', 'DELETED')),
    CONSTRAINT chk_template_global_tenant CHECK (
        (is_global = true AND tenant_id IS NULL) OR 
        (is_global = false AND tenant_id IS NOT NULL)
    ),
    CONSTRAINT uq_template_code_tenant UNIQUE (code, tenant_id)
);

-- Indexes
CREATE INDEX idx_email_templates_tenant_id ON email_templates(tenant_id);
CREATE INDEX idx_email_templates_code ON email_templates(code);
CREATE INDEX idx_email_templates_type ON email_templates(type);
CREATE INDEX idx_email_templates_is_global ON email_templates(is_global);
CREATE INDEX idx_email_templates_is_active ON email_templates(is_active);

-- Composite indexes
CREATE INDEX idx_email_templates_tenant_active ON email_templates(tenant_id, is_active);
CREATE INDEX idx_email_templates_global_active ON email_templates(is_global, is_active) WHERE is_global = true;

-- JSONB indexes
CREATE INDEX idx_email_templates_variables_schema ON email_templates USING GIN(variables_schema);

-- Comments
COMMENT ON TABLE email_templates IS 'Email templates using Thymeleaf syntax';
COMMENT ON COLUMN email_templates.code IS 'Unique template code identifier';
COMMENT ON COLUMN email_templates.body_template IS 'Thymeleaf template content';
COMMENT ON COLUMN email_templates.variables_schema IS 'JSON schema defining required variables';
COMMENT ON COLUMN email_templates.is_global IS 'True for system-wide templates, false for tenant-specific';
