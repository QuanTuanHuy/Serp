/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

-- ======================================
-- V1: Create emails table
-- ======================================

CREATE TABLE IF NOT EXISTS emails (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT,
    
    -- Email provider and status
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    type VARCHAR(50) NOT NULL,
    
    -- Email addresses
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    reply_to VARCHAR(255),
    to_emails TEXT[] NOT NULL,
    cc_emails TEXT[],
    bcc_emails TEXT[],
    
    -- Email content
    subject VARCHAR(500) NOT NULL,
    body TEXT,
    is_html BOOLEAN DEFAULT true,
    
    -- Template
    template_id BIGINT,
    template_variables JSONB,
    
    -- Metadata and tracking
    metadata JSONB,
    provider_message_id VARCHAR(255),
    provider_response JSONB,
    
    -- Retry logic
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    next_retry_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    error_message TEXT,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    CONSTRAINT chk_email_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'RETRY', 'CANCELLED')),
    CONSTRAINT chk_email_provider CHECK (provider IN ('JAVA_MAIL', 'BREVO')),
    CONSTRAINT chk_email_priority CHECK (priority IN ('HIGH', 'NORMAL', 'LOW')),
    -- CONSTRAINT chk_email_type CHECK (type IN ('VERIFICATION', 'NOTIFICATION', 'MARKETING', 'TRANSACTIONAL', 'PASSWORD_RESET', 'ALERT', 'REMINDER', 'WELCOME')),
    CONSTRAINT chk_active_status CHECK (active_status IN ('ACTIVE', 'DELETED'))
);

-- Indexes for performance
CREATE INDEX idx_emails_tenant_id ON emails(tenant_id);
CREATE INDEX idx_emails_user_id ON emails(user_id);
CREATE INDEX idx_emails_message_id ON emails(message_id);
CREATE INDEX idx_emails_status ON emails(status);
CREATE INDEX idx_emails_provider ON emails(provider);
CREATE INDEX idx_emails_type ON emails(type);
CREATE INDEX idx_emails_created_at ON emails(created_at DESC);
CREATE INDEX idx_emails_sent_at ON emails(sent_at DESC);

-- Composite indexes for common queries
CREATE INDEX idx_emails_tenant_status ON emails(tenant_id, status);
CREATE INDEX idx_emails_tenant_created ON emails(tenant_id, created_at DESC);
CREATE INDEX idx_emails_status_retry ON emails(status, next_retry_at) WHERE status = 'RETRY';

-- JSONB indexes for metadata queries
CREATE INDEX idx_emails_metadata ON emails USING GIN(metadata);
CREATE INDEX idx_emails_template_variables ON emails USING GIN(template_variables);

-- Comment on table
COMMENT ON TABLE emails IS 'Stores all email records sent through the mail service';
COMMENT ON COLUMN emails.message_id IS 'Unique identifier for email message (UUID)';
COMMENT ON COLUMN emails.tenant_id IS 'Tenant/Organization ID for multi-tenancy';
COMMENT ON COLUMN emails.provider IS 'Email provider used (JAVA_MAIL or BREVO)';
COMMENT ON COLUMN emails.status IS 'Current status of email';
COMMENT ON COLUMN emails.retry_count IS 'Number of retry attempts made';
COMMENT ON COLUMN emails.metadata IS 'Additional metadata stored as JSON';
