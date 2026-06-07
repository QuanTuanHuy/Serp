-- Optimistic locking for rep_time_blocks (aligns with JPA @Version on RepTimeBlockModel).

ALTER TABLE rep_time_blocks
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
