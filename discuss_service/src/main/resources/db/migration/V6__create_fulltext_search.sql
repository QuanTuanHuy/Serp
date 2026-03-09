/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create full-text search indexes and generated columns
*/

-- ====================================
-- Full-Text Search Setup for Messages
-- ====================================

-- Add generated column for full-text search
ALTER TABLE messages 
    ADD COLUMN search_vector tsvector 
    GENERATED ALWAYS AS (to_tsvector('english', content)) STORED;

-- Create GIN index for full-text search
CREATE INDEX idx_messages_search 
    ON messages USING GIN(search_vector);

-- Add comment for documentation
COMMENT ON COLUMN messages.search_vector IS 'Generated column for full-text search on message content';
