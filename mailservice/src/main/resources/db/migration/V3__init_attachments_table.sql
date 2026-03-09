/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

-- ======================================
-- V3: Create email_attachments table
-- ======================================

CREATE TABLE IF NOT EXISTS email_attachments (
    id BIGSERIAL PRIMARY KEY,
    email_id BIGINT NOT NULL,
    
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    
    storage_location VARCHAR(50) DEFAULT 'LOCAL',
    checksum VARCHAR(64),
    
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    CONSTRAINT fk_email_attachments_email FOREIGN KEY (email_id) 
        REFERENCES emails(id) ON DELETE CASCADE,
    CONSTRAINT chk_attachment_active_status CHECK (active_status IN ('ACTIVE', 'DELETED'))
);

-- Indexes
CREATE INDEX idx_email_attachments_email_id ON email_attachments(email_id);
CREATE INDEX idx_email_attachments_stored_filename ON email_attachments(stored_filename);
CREATE INDEX idx_email_attachments_expires_at ON email_attachments(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_email_attachments_uploaded_at ON email_attachments(uploaded_at DESC);

-- Comments
COMMENT ON TABLE email_attachments IS 'Email attachment metadata (files stored on filesystem)';
COMMENT ON COLUMN email_attachments.stored_filename IS 'Unique filename used in storage (UUID-based)';
COMMENT ON COLUMN email_attachments.file_path IS 'Absolute or relative path to stored file';
COMMENT ON COLUMN email_attachments.storage_location IS 'Storage backend (LOCAL, S3, AZURE_BLOB)';
COMMENT ON COLUMN email_attachments.checksum IS 'SHA-256 checksum for file integrity';
COMMENT ON COLUMN email_attachments.expires_at IS 'Expiration date for attachment cleanup (7 days default)';
