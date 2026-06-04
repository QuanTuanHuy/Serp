/*
Author: Nguyen The Anh
Description: Part of Serp Project - Add route origin endpoint model
*/

ALTER TABLE routes
    ADD COLUMN IF NOT EXISTS origin_type VARCHAR(30) NOT NULL DEFAULT 'HUB';

ALTER TABLE routes
    ADD COLUMN IF NOT EXISTS origin_post_office_code VARCHAR(255);

ALTER TABLE routes
    ALTER COLUMN origin_hub_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_routes_origin_type ON routes(origin_type);
CREATE INDEX IF NOT EXISTS idx_routes_origin_post_office_code ON routes(origin_post_office_code);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_routes_origin_hub_or_post_office'
    ) THEN
        ALTER TABLE routes
            ADD CONSTRAINT ck_routes_origin_hub_or_post_office
            CHECK (
                (origin_type = 'HUB' AND origin_hub_id IS NOT NULL AND origin_post_office_code IS NULL)
                OR
                (origin_type = 'POST_OFFICE' AND origin_hub_id IS NULL AND origin_post_office_code IS NOT NULL)
            );
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_routes_supported_endpoint_pair'
    ) THEN
        ALTER TABLE routes
            ADD CONSTRAINT ck_routes_supported_endpoint_pair
            CHECK (
                origin_type = 'HUB'
                OR
                (origin_type = 'POST_OFFICE' AND destination_type = 'HUB')
            );
    END IF;
END $$;

COMMENT ON COLUMN routes.origin_type IS 'Route origin endpoint type: HUB or POST_OFFICE';
COMMENT ON COLUMN routes.origin_post_office_code IS 'Origin post office code for PO-to-Hub routes';
