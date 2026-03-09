/*
Author: QuanTuanHuy
Description: Part of Serp Project - Refactor attachment storage fields from S3-specific to generic
*/

-- Add storage_provider column to track which storage backend is used
ALTER TABLE attachments ADD COLUMN storage_provider VARCHAR(20) DEFAULT 'S3';

-- Rename s3_bucket to storage_bucket
ALTER TABLE attachments RENAME COLUMN s3_bucket TO storage_bucket;

-- Rename s3_key to storage_key
ALTER TABLE attachments RENAME COLUMN s3_key TO storage_key;

-- Rename s3_url to storage_url
ALTER TABLE attachments RENAME COLUMN s3_url TO storage_url;

-- Add constraint for valid storage providers
ALTER TABLE attachments ADD CONSTRAINT valid_storage_provider 
    CHECK (storage_provider IN ('S3', 'GCS', 'AZURE_BLOB', 'LOCAL'));

-- Add index on storage_provider for potential filtering
CREATE INDEX idx_attachments_storage_provider ON attachments(storage_provider);

-- Add comment for documentation
COMMENT ON COLUMN attachments.storage_provider IS 'Storage provider: S3 (MinIO/AWS), GCS, AZURE_BLOB, LOCAL';
COMMENT ON COLUMN attachments.storage_bucket IS 'Storage bucket/container name';
COMMENT ON COLUMN attachments.storage_key IS 'Object key/path within the bucket';
COMMENT ON COLUMN attachments.storage_url IS 'Public or presigned URL to access the file';
