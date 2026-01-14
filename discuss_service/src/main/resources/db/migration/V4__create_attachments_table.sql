/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create attachments table
*/

CREATE TABLE attachments (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    
    -- Foreign Keys
    message_id BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    channel_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    
    -- File Info
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL, -- bytes
    file_type VARCHAR(100),    -- MIME type
    file_extension VARCHAR(20),
    
    -- Storage
    s3_bucket VARCHAR(255),
    s3_key VARCHAR(500) NOT NULL,
    s3_url TEXT NOT NULL,
    
    -- Preview (for images)
    thumbnail_url TEXT,
    width INT,
    height INT,
    
    -- Virus Scan
    scan_status VARCHAR(50) DEFAULT 'PENDING',
    -- Values: 'PENDING', 'CLEAN', 'INFECTED', 'ERROR'
    scanned_at TIMESTAMP,
    
    -- Metadata
    metadata JSONB,
    -- Example: {"duration": 120, "codec": "h264"} for videos
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT valid_scan_status CHECK (
        scan_status IN ('PENDING', 'CLEAN', 'INFECTED', 'ERROR')
    ),
    CONSTRAINT positive_file_size CHECK (file_size > 0)
);

-- Basic indexes
CREATE INDEX idx_attachments_message 
    ON attachments(message_id);

CREATE INDEX idx_attachments_channel 
    ON attachments(channel_id, created_at DESC);

CREATE INDEX idx_attachments_tenant_type 
    ON attachments(tenant_id, file_type, created_at DESC);

CREATE INDEX idx_attachments_scan 
    ON attachments(scan_status) 
    WHERE scan_status = 'PENDING';
