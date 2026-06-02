/*
Author: Nguyen The Anh
Description: Part of Serp Project - Link post office to Hub (second-mile hub id, logical reference)
*/

ALTER TABLE post_offices
    ADD COLUMN IF NOT EXISTS hub_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_post_offices_hub_id ON post_offices (hub_id);

COMMENT ON COLUMN post_offices.hub_id IS 'Hub id managed in second-mile; optional assignment under hub';
