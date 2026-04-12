/*
Author: QuanTuanHuy
Description: Part of Serp Project - Add pg_trgm index for message content search fallback
*/

-- Enable trigram extension for fast ILIKE/partial text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Trigram index for case-insensitive content matching (used by LOWER(content) LIKE ...)
CREATE INDEX IF NOT EXISTS idx_messages_content_trgm
    ON messages USING GIN (LOWER(content) gin_trgm_ops)
    WHERE is_deleted = FALSE AND content IS NOT NULL;
