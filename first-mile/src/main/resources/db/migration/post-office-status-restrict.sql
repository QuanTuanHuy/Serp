ALTER TABLE post_offices
    DROP CONSTRAINT IF EXISTS chk_post_offices_status;

UPDATE post_offices
SET status = 'INACTIVE'
WHERE status NOT IN ('ACTIVE', 'INACTIVE');

ALTER TABLE post_offices
    ADD CONSTRAINT chk_post_offices_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'));