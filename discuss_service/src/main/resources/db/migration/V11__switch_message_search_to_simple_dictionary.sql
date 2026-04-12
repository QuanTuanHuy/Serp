/*
Author: QuanTuanHuy
Description: Part of Serp Project - Switch message full-text search from english to simple dictionary
*/

-- Use simple dictionary to improve multilingual matching (including Vietnamese terms)
-- and avoid english stemming/stop-word behavior for chat content.

-- Recreate search index/column with new generated expression
DROP INDEX IF EXISTS idx_messages_search;

ALTER TABLE messages DROP COLUMN IF EXISTS search_vector;

ALTER TABLE messages
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('simple', COALESCE(content, ''))) STORED;

CREATE INDEX idx_messages_search
    ON messages USING GIN(search_vector);

COMMENT ON COLUMN messages.search_vector
    IS 'Generated column for full-text search on message content using simple dictionary';
