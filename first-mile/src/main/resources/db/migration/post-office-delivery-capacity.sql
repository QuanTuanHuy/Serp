-- Author: Nguyen The Anh
-- Description: Add destination delivery load fields for post offices.

ALTER TABLE post_offices
    ADD COLUMN IF NOT EXISTS delivery_capacity INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS current_delivery_load INTEGER NOT NULL DEFAULT 0;

UPDATE post_offices
SET delivery_capacity = daily_capacity
WHERE delivery_capacity = 0
  AND daily_capacity > 0;
