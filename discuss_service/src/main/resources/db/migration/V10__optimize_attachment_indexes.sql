/*
Author: QuanTuanHuy
Description: Part of Serp Project - Optimize attachment indexes for batch loading
*/

-- Drop obsolete scan status index (scan columns were removed in V8)
DROP INDEX IF EXISTS idx_attachments_scan;

-- Drop obsolete scan status constraint (columns no longer exist)
ALTER TABLE attachments DROP CONSTRAINT IF EXISTS valid_scan_status;

-- The message_id index already exists from V4, but we can verify it's optimal
-- for batch loading (which uses WHERE message_id IN (...))
-- idx_attachments_message ON attachments(message_id) already exists

-- Add composite index for common query pattern: get attachments by channel + message
-- This is useful when loading messages for a channel with their attachments
CREATE INDEX IF NOT EXISTS idx_attachments_channel_message 
    ON attachments(channel_id, message_id);
