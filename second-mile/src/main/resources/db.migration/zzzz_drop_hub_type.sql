-- Author: Nguyen The Anh
-- Description: Remove hub type from hub master data

ALTER TABLE hubs
    DROP COLUMN IF EXISTS hub_type;
