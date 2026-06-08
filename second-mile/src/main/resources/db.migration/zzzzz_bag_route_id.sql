/*
Author: Nguyen The Anh
Description: Part of Serp Project - Add route assignment to bags
*/

ALTER TABLE bags
    ADD COLUMN IF NOT EXISTS route_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_bags_route_id ON bags(route_id);
